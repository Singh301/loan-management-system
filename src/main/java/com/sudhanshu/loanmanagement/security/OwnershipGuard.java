package com.sudhanshu.loanmanagement.security;

import com.sudhanshu.loanmanagement.customer.entity.Customer;
import com.sudhanshu.loanmanagement.customer.repository.CustomerRepository;
import com.sudhanshu.loanmanagement.exception.AccessDeniedException;
import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Enforces customer data isolation and role-based ownership checks.
 */
@Component
@RequiredArgsConstructor
public class OwnershipGuard {

    private final CustomerRepository customerRepository;

    public Authentication requireAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new AccessDeniedException("Authentication required.");
        }
        return auth;
    }

    public boolean isAdminOrManager(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String r = a.getAuthority();
                    return "ROLE_ADMIN".equals(r) || "ROLE_MANAGER".equals(r)
                            || Role.ROLE_ADMIN.name().equals(r)
                            || Role.ROLE_MANAGER.name().equals(r);
                });
    }

    public boolean isCustomer(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String r = a.getAuthority();
                    return "ROLE_CUSTOMER".equals(r) || Role.ROLE_CUSTOMER.name().equals(r);
                });
    }

    public Customer requireCustomerForUsername(String username) {
        return customerRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found for logged in user."));
    }

    /**
     * Customers may only access their own customerId.
     * Admin/Manager may access any.
     */
    public void assertCanAccessCustomer(Long customerId) {
        Authentication auth = requireAuthentication();
        if (isAdminOrManager(auth)) {
            return;
        }
        Customer me = requireCustomerForUsername(auth.getName());
        if (!me.getCustomerId().equals(customerId)) {
            throw new AccessDeniedException("You are not allowed to access this customer data.");
        }
    }

    /**
     * Customers may only access loans they own.
     */
    public void assertCanAccessLoan(Loan loan) {
        Authentication auth = requireAuthentication();
        if (isAdminOrManager(auth)) {
            return;
        }
        String loanUsername = loan.getCustomer().getUser().getUsername();
        if (!auth.getName().equals(loanUsername)) {
            throw new AccessDeniedException("You are not allowed to access this loan.");
        }
    }

    public Long currentUserIdIfPresent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails details)) {
            return null;
        }
        return details.getUser().getUserId();
    }

    public String currentUsername() {
        return requireAuthentication().getName();
    }
}
