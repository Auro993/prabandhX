package com.prabandhx.prabandhx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Organization organization;

    // ===== POINTS SYSTEM FIELDS =====
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;
    
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;
    
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;
    
    @Column(name = "last_completed_date")
    private LocalDateTime lastCompletedDate;

    // ===== NEW: Guest/Collaborator fields =====
    @Column(name = "is_guest")
    private Boolean isGuest = false;

    @Column(name = "guest_expiry")
    private LocalDateTime guestExpiry;

    @OneToMany(mappedBy = "invitedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Collaborator> sentInvitations;

    // ===== COMMENTED OUT - CAUSING HIBERNATE ERROR =====
    // @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnore
    // private List<Collaborator> receivedInvitations;

    // ===== CONSTRUCTORS =====
    
    public User() {
        this.totalPoints = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.isGuest = false;
    }

    public User(String name, String email, String password, String role, Organization organization) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.organization = organization;
        this.totalPoints = 0;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.isGuest = false;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    // ===== POINTS GETTERS & SETTERS =====
    
    public Integer getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDateTime getLastCompletedDate() {
        return lastCompletedDate;
    }

    public void setLastCompletedDate(LocalDateTime lastCompletedDate) {
        this.lastCompletedDate = lastCompletedDate;
    }

    // ===== NEW: Guest/Collaborator getters & setters =====

    public Boolean getIsGuest() {
        return isGuest;
    }

    public void setIsGuest(Boolean isGuest) {
        this.isGuest = isGuest;
    }

    public LocalDateTime getGuestExpiry() {
        return guestExpiry;
    }

    public void setGuestExpiry(LocalDateTime guestExpiry) {
        this.guestExpiry = guestExpiry;
    }

    public List<Collaborator> getSentInvitations() {
        return sentInvitations;
    }

    public void setSentInvitations(List<Collaborator> sentInvitations) {
        this.sentInvitations = sentInvitations;
    }

    // ===== COMMENTED OUT GETTERS/SETTERS =====
    /*
    public List<Collaborator> getReceivedInvitations() {
        return receivedInvitations;
    }

    public void setReceivedInvitations(List<Collaborator> receivedInvitations) {
        this.receivedInvitations = receivedInvitations;
    }
    */

    // ===== HELPER METHODS =====
    
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getOrganizationName() {
        return organization != null ? organization.getName() : null;
    }

    // ===== NEW: Collaborator helper methods =====

    public boolean isGuest() {
        return isGuest != null && isGuest;
    }

    public boolean isGuestExpired() {
        return guestExpiry != null && guestExpiry.isBefore(LocalDateTime.now());
    }

    public boolean isActiveGuest() {
        return isGuest() && !isGuestExpired();
    }

    public int getSentInvitationsCount() {
        return sentInvitations != null ? sentInvitations.size() : 0;
    }

    // ===== COMMENTED OUT HELPER METHODS =====
    /*
    public int getReceivedInvitationsCount() {
        return receivedInvitations != null ? receivedInvitations.size() : 0;
    }

    public int getAcceptedInvitationsCount() {
        if (receivedInvitations == null) return 0;
        return (int) receivedInvitations.stream()
                .filter(Collaborator::getIsAccepted)
                .count();
    }

    public boolean hasPendingInvitations() {
        if (receivedInvitations == null) return false;
        return receivedInvitations.stream()
                .anyMatch(i -> !i.getIsAccepted() && i.getIsActive());
    }
    */

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isGuest=" + isGuest +
                ", totalPoints=" + totalPoints +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                ", organizationId=" + getOrganizationId() +
                '}';
    }
}