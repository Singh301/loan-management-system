#!/usr/bin/env bash
# One-time AWS infrastructure setup for Loan Management System
# Prerequisites: AWS CLI configured, jq installed
# Usage: ./aws/setup-aws-infra.sh [region] [account-id]

set -euo pipefail

REGION="${1:-ap-south-1}"
ACCOUNT_ID="${2:-$(aws sts get-caller-identity --query Account --output text)}"
ECR_REPO="loan-management-system"
CLUSTER="loan-cluster"
SERVICE="loan-service"
TASK_FAMILY="loan-task"
LOG_GROUP="/ecs/loan-management-system"

echo "==> Region: $REGION | Account: $ACCOUNT_ID"

echo "==> Creating ECR repository (if not exists)..."
aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$REGION" 2>/dev/null \
  || aws ecr create-repository --repository-name "$ECR_REPO" --region "$REGION" \
       --image-scanning-configuration scanOnPush=true

echo "==> Creating CloudWatch log group..."
aws logs create-log-group --log-group-name "$LOG_GROUP" --region "$REGION" 2>/dev/null || true

echo "==> Creating ECS cluster..."
aws ecs describe-clusters --clusters "$CLUSTER" --region "$REGION" \
  --query "clusters[0].status" --output text 2>/dev/null | grep -q ACTIVE \
  || aws ecs create-cluster --cluster-name "$CLUSTER" --region "$REGION" \
       --capacity-providers FARGATE FARGATE_SPOT \
       --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1

echo "==> Ensure SSM parameters exist (you must set values)..."
for PARAM in DB_URL DB_USERNAME DB_PASSWORD JWT_SECRET; do
  NAME="/loan/$PARAM"
  if ! aws ssm get-parameter --name "$NAME" --region "$REGION" &>/dev/null; then
    echo "    Creating placeholder: $NAME (UPDATE WITH REAL VALUE)"
    aws ssm put-parameter --name "$NAME" --value "CHANGE_ME" --type SecureString --region "$REGION" || true
  else
    echo "    Exists: $NAME"
  fi
done

echo ""
echo "==> Next steps (manual once):"
echo "1. Create RDS MySQL (db.t3.micro is fine for demo) in private subnet"
echo "2. Update SSM params:"
echo "     aws ssm put-parameter --name /loan/DB_URL --value 'jdbc:mysql://YOUR_RDS_ENDPOINT:3306/loan_management_system' --type SecureString --overwrite --region $REGION"
echo "     aws ssm put-parameter --name /loan/DB_USERNAME --value 'admin' --type SecureString --overwrite --region $REGION"
echo "     aws ssm put-parameter --name /loan/DB_PASSWORD --value 'YourStrongPassword' --type SecureString --overwrite --region $REGION"
echo "     aws ssm put-parameter --name /loan/JWT_SECRET --value 'your-64-char-random-secret-key-here-minimum' --type SecureString --overwrite --region $REGION"
echo "3. Create VPC, subnets, security groups (app SG allow 8080 from ALB, RDS SG allow 3306 from app SG)"
echo "4. Create ALB + target group (port 8080, health /actuator/health)"
echo "5. Replace ACCOUNT_ID in aws/ecs-task-definition.json and register:"
echo "     sed \"s/ACCOUNT_ID/$ACCOUNT_ID/g\" aws/ecs-task-definition.json > /tmp/task-def.json"
echo "     aws ecs register-task-definition --cli-input-json file:///tmp/task-def.json --region $REGION"
echo "6. Create ECS service with Fargate, attach to ALB target group"
echo "7. Configure GitHub secrets: AWS_ROLE_ARN (OIDC) or AWS_ACCESS_KEY_ID + AWS_SECRET_ACCESS_KEY"
echo "8. Configure GitHub variables: AWS_REGION, ECR_REPOSITORY, ECS_CLUSTER, ECS_SERVICE, ECS_TASK_DEFINITION"
echo ""
echo "Done (bootstrap)."
