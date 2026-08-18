# AWS Deployment Guide – Loan Management System

## Architecture (recommended for portfolio / small prod)

```
GitHub (push to main)
    │
    ▼
GitHub Actions CD
    │  build Docker image
    │  push → Amazon ECR
    │  update ECS task definition
    ▼
Amazon ECS Fargate  (service: loan-service)
    │
    ├─ Application Load Balancer (HTTPS)
    ├─ RDS MySQL 8 (private subnet)
    ├─ SSM Parameter Store (secrets)
    └─ CloudWatch Logs
```

**Cost tip (demo):** Fargate 0.5 vCPU / 1 GB + db.t3.micro + ALB ≈ low monthly cost if stopped when idle.

---

## Prerequisites

1. AWS account + CLI configured (`aws configure` or SSO)
2. GitHub repo with this code
3. Domain (optional) + ACM certificate for HTTPS

---

## Step 1 – One-time AWS bootstrap

```bash
# From project root
chmod +x aws/setup-aws-infra.sh
./aws/setup-aws-infra.sh ap-south-1
```

This creates:
- ECR repository `loan-management-system`
- ECS cluster `loan-cluster`
- CloudWatch log group `/ecs/loan-management-system`
- SSM parameter placeholders under `/loan/*`

---

## Step 2 – Database (RDS)

1. Create **RDS MySQL 8** in a **private subnet**
2. Security group: allow inbound **3306** only from the ECS task security group
3. Create database: `loan_management_system`
4. Store connection in SSM:

```bash
aws ssm put-parameter --name /loan/DB_URL \
  --value 'jdbc:mysql://YOUR_RDS_ENDPOINT:3306/loan_management_system' \
  --type SecureString --overwrite --region ap-south-1

aws ssm put-parameter --name /loan/DB_USERNAME --value 'admin' \
  --type SecureString --overwrite --region ap-south-1

aws ssm put-parameter --name /loan/DB_PASSWORD --value 'YourStrongPassword' \
  --type SecureString --overwrite --region ap-south-1

aws ssm put-parameter --name /loan/JWT_SECRET \
  --value 'REPLACE_WITH_64_PLUS_CHAR_RANDOM_SECRET' \
  --type SecureString --overwrite --region ap-south-1
```

Flyway runs on app startup and applies `V1`–`V9`.

---

## Step 3 – Networking & Load Balancer

1. **VPC** with public + private subnets (2 AZs)
2. **ALB** in public subnets
3. **Target group**: port `8080`, health check path `/actuator/health`
4. **Security groups**
   - ALB SG: inbound 80/443 from internet
   - App SG: inbound 8080 from ALB SG only
   - RDS SG: inbound 3306 from App SG only
5. (Optional) ACM certificate + HTTPS listener

---

## Step 4 – IAM roles

### ECS Task Execution Role (`ecsTaskExecutionRole`)
Must allow:
- `ecr:GetAuthorizationToken`, pull images
- `logs:CreateLogStream`, `logs:PutLogEvents`
- `ssm:GetParameters` for `/loan/*`
- `kms:Decrypt` if using SecureString with CMK

### ECS Task Role (`ecsTaskRole`)
App runtime permissions (minimal for now; expand if using S3 for documents later).

### GitHub OIDC role (recommended over long-lived access keys)
1. Create OIDC provider for `token.actions.githubusercontent.com`
2. Role trust policy limited to your repo
3. Permissions: ECR push, ECS update service, describe task definition

---

## Step 5 – Register task definition

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
sed "s/ACCOUNT_ID/$ACCOUNT_ID/g" aws/ecs-task-definition.json > /tmp/task-def.json
aws ecs register-task-definition --cli-input-json file:///tmp/task-def.json --region ap-south-1
```

---

## Step 6 – Create ECS service

```bash
aws ecs create-service \
  --cluster loan-cluster \
  --service-name loan-service \
  --task-definition loan-task \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-app],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:...:targetgroup/...,containerName=loan-management-system,containerPort=8080" \
  --region ap-south-1
```

Use private subnets + NAT for outbound (ECR pull, SSM). For cheapest demo only, `assignPublicIp=ENABLED` in public subnet works without NAT.

---

## Step 7 – GitHub Actions secrets & variables

### Secrets
| Name | Value |
|------|--------|
| `AWS_ROLE_ARN` | OIDC role ARN (preferred) |
| **or** `AWS_ACCESS_KEY_ID` + `AWS_SECRET_ACCESS_KEY` | IAM user keys (less secure) |

If using access keys, edit `.github/workflows/cd-aws.yml` and switch the credential step as commented.

### Variables (Settings → Secrets and variables → Actions → Variables)
| Name | Example |
|------|---------|
| `AWS_REGION` | `ap-south-1` |
| `ECR_REPOSITORY` | `loan-management-system` |
| `ECS_CLUSTER` | `loan-cluster` |
| `ECS_SERVICE` | `loan-service` |
| `ECS_TASK_DEFINITION` | `loan-task` |

---

## Step 8 – Deploy

Push to `main` / `master` **or** run workflow manually:

```
Actions → CD - Deploy to AWS ECS → Run workflow
```

Pipeline:
1. Build multi-stage Docker image  
2. Push to ECR (`:git-sha` and `:latest`)  
3. Render new task definition with new image  
4. Deploy ECS service and wait for stability  

---

## Verify

```bash
# Service status
aws ecs describe-services --cluster loan-cluster --services loan-service --region ap-south-1

# Logs
aws logs tail /ecs/loan-management-system --follow --region ap-south-1

# Health via ALB
curl -s https://YOUR_ALB_DNS/actuator/health
curl -s https://YOUR_ALB_DNS/swagger-ui.html
```

---

## CI only (no deploy)

Every push/PR runs `.github/workflows/ci.yml`:
- JDK 21 setup  
- `mvn compile` + `package`  
- JAR artifact uploaded  

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Task stops immediately | Check CloudWatch logs; usually bad DB_URL / JWT_SECRET / Flyway |
| Cannot pull image | Execution role missing ECR permissions |
| Health check fails | Ensure `/actuator/health` is permitted without auth in `SecurityConfig` |
| SSM secrets empty | Execution role needs `ssm:GetParameters` on `/loan/*` |
| OIDC assume role fails | Trust policy must match repo + branch |

---

## Alternative: simpler deploy (EC2 + Docker)

If you prefer one EC2 instance:

```bash
# On EC2 (Amazon Linux 2023)
sudo yum install -y docker
sudo systemctl enable --now docker
aws ecr get-login-password --region ap-south-1 | sudo docker login --username AWS --password-stdin ACCOUNT.dkr.ecr.ap-south-1.amazonaws.com
sudo docker run -d --name loan -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... -e JWT_SECRET=... \
  ACCOUNT.dkr.ecr.ap-south-1.amazonaws.com/loan-management-system:latest
```

ECS Fargate is still preferred for resume/interview credibility.

---

## Security checklist before go-live

- [ ] Strong `JWT_SECRET` (64+ random chars) in SSM only  
- [ ] RDS not publicly accessible  
- [ ] ALB HTTPS only (redirect HTTP → HTTPS)  
- [ ] Actuator endpoints restricted in prod if needed  
- [ ] GitHub uses OIDC, not long-lived access keys  
- [ ] Image scanning enabled on ECR  
