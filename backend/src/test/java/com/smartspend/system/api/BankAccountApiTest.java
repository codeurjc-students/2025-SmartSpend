package com.smartspend.system.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.smartspend.bankAccount.BankAccount;
import com.smartspend.bankAccount.BankAccountController;
import com.smartspend.bankAccount.BankAccountService;
import com.smartspend.bankAccount.dtos.CreateBankAccountDTO;

class BankAccountApiTest {

    private MockMvc mockMvc;

    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController bankAccountController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(bankAccountController).build();
    }

    @Test
    void shouldGetAccountsByAuthenticatedUser() throws Exception {
        BankAccount account = new BankAccount();
        account.setId(1L);
        account.setAccountName("Principal");
        account.setCurrentBalance(new BigDecimal("250.00"));

        when(bankAccountService.getUserBankAccountsByEmail("user@test.com")).thenReturn(List.of(account));

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(get("/api/v1/accounts").principal(authentication))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].accountName").value("Principal"));

        verify(bankAccountService).getUserBankAccountsByEmail("user@test.com");
    }

    @Test
    void shouldCreateAccount() throws Exception {
        BankAccount account = new BankAccount();
        account.setId(9L);
        account.setAccountName("Ahorro");
        account.setCurrentBalance(new BigDecimal("1000.00"));

        when(bankAccountService.createBankAccount(new CreateBankAccountDTO("Ahorro", new BigDecimal("1000.00")), "user@test.com"))
            .thenReturn(account);

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        String body = """
            {
              \"accountName\": \"Ahorro\",
              \"initialBalance\": 1000.00
            }
            """;

        mockMvc.perform(post("/api/v1/accounts")
                .principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(9))
            .andExpect(jsonPath("$.accountName").value("Ahorro"));

        verify(bankAccountService).createBankAccount(new CreateBankAccountDTO("Ahorro", new BigDecimal("1000.00")), "user@test.com");
    }

    @Test
    void shouldDeleteAccount() throws Exception {
        BankAccount account = new BankAccount();
        account.setId(22L);

        when(bankAccountService.getBankAccountByIdAndEmail(22L, "user@test.com")).thenReturn(account);

        Authentication authentication = new UsernamePasswordAuthenticationToken("user@test.com", null);

        mockMvc.perform(delete("/api/v1/accounts/22").principal(authentication))
            .andExpect(status().isNoContent());

        verify(bankAccountService).getBankAccountByIdAndEmail(22L, "user@test.com");
        verify(bankAccountService).deleteBankAccount(account);
    }
}
