package com.smartspend.user;

import java.util.List;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.category.Category;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Builder
@AllArgsConstructor
@Table(name="users")
@Getter @Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; 

    @Column(nullable=false)
    private String userName;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String userHashedPassword;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<BankAccount> userBankAccounts;

    @OneToMany(mappedBy = "user")
    @JsonBackReference
    private List<Category> userCategories;

    @Column(name = "privacy_policy_accepted", nullable = false)
    @Builder.Default
    private Boolean privacyPolicyAccepted = false;


    // constructors
    public User(){
        this.privacyPolicyAccepted = false;
    }

    public User(String userName, String userEmail, String userHashedPassword){
        this.userName = userName;
        this.userEmail = userEmail;
        this.userHashedPassword = userHashedPassword;

    }




}
