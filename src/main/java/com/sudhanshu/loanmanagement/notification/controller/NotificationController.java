package com.sudhanshu.loanmanagement.notification.controller;

import com.sudhanshu.loanmanagement.constants.ApiConstants;
import com.sudhanshu.loanmanagement.dto.ApiResponse;
import com.sudhanshu.loanmanagement.notification.entity.Notification;
import com.sudhanshu.loanmanagement.notification.service.NotificationService;
import com.sudhanshu.loanmanagement.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.API_V1 + "/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse> getMyNotifications(Authentication authentication) {
        Long userId = ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserId();
        List<Notification> list = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Notifications fetched successfully")
                .data(list)
                .build());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark as read")
    public ResponseEntity<ApiResponse> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Notification marked as read")
                .data(null)
                .build());
    }
}