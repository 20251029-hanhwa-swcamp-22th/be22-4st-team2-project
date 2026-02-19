package com.salesboost;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salesboost.domain.admin.entity.AdminUser;
import com.salesboost.domain.admin.repository.AdminUserRepository;
import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.entity.InquiryType;
import com.salesboost.domain.inquiry.repository.InquiryRepository;
import com.salesboost.domain.portfolio.entity.Portfolio;
import com.salesboost.domain.portfolio.repository.PortfolioRepository;
import java.lang.reflect.Constructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // 모든 테스트는 이 관리자 계정으로 인증을 수행한다.
        AdminUser admin = newInstance(AdminUser.class);
        ReflectionTestUtils.setField(admin, "username", "admin");
        ReflectionTestUtils.setField(admin, "password", passwordEncoder.encode("admin1234!"));
        ReflectionTestUtils.setField(admin, "role", "ROLE_ADMIN");
        ReflectionTestUtils.setField(admin, "enabled", true);
        adminUserRepository.save(admin);
    }

    @Test
    void loginReturnsAccessToken() throws Exception {
        // FR-11: 관리자 로그인 시 JWT 액세스 토큰이 발급되어야 한다.
        mockMvc.perform(post("/api/admin/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin",
                                  "password":"admin1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void corsPreflightRequestIsAllowedForFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/portfolios")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
    }

    @Test
    void inquiryAdminApisWorkWithJwt() throws Exception {
        // FR-13~16: 문의 목록/상세/상태 변경/메모 수정 API가 JWT 인증으로 동작해야 한다.
        Long inquiryId = createInquiry();
        String token = issueToken();

        mockMvc.perform(get("/api/admin/inquiries")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(inquiryId));

        mockMvc.perform(get("/api/admin/inquiries/{id}", inquiryId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(inquiryId));

        mockMvc.perform(patch("/api/admin/inquiries/{id}/status", inquiryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "status":"DONE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(patch("/api/admin/inquiries/{id}/memo", inquiryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "memo":"completed"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Inquiry updated = inquiryRepository.findById(inquiryId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(InquiryStatus.DONE);
        assertThat(updated.getAdminMemo()).isEqualTo("completed");
    }

    @Test
    void portfolioAdminVisibilityAndOrderApisWorkWithJwt() throws Exception {
        // FR-20: 관리자 API로 포트폴리오 노출 여부와 노출 순서를 변경할 수 있어야 한다.
        Long firstId = createPortfolio("first");
        Long secondId = createPortfolio("second");
        String token = issueToken();

        mockMvc.perform(get("/api/admin/portfolios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(patch("/api/admin/portfolios/{id}/visibility", firstId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "visible":false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(patch("/api/admin/portfolios/order")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "portfolioIds":[%d,%d]
                                }
                                """.formatted(secondId, firstId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Portfolio first = portfolioRepository.findById(firstId).orElseThrow();
        Portfolio second = portfolioRepository.findById(secondId).orElseThrow();
        assertThat(first.isVisible()).isFalse();
        assertThat(second.getDisplayOrder()).isEqualTo(1);
        assertThat(first.getDisplayOrder()).isEqualTo(2);
    }

    private Long createInquiry() {
        // 관리자 문의 API 테스트를 위한 최소 문의 데이터 생성
        Inquiry inquiry = newInstance(Inquiry.class);
        ReflectionTestUtils.setField(inquiry, "companyName", "Acme");
        ReflectionTestUtils.setField(inquiry, "contactName", "Kim");
        ReflectionTestUtils.setField(inquiry, "email", "kim@acme.com");
        ReflectionTestUtils.setField(inquiry, "phone", "010-1234-5678");
        ReflectionTestUtils.setField(inquiry, "inquiryType", InquiryType.PARTNERSHIP);
        ReflectionTestUtils.setField(inquiry, "content", "Need collaboration");
        ReflectionTestUtils.setField(inquiry, "status", InquiryStatus.PENDING);
        return inquiryRepository.save(inquiry).getId();
    }

    private Long createPortfolio(String title) {
        // 관리자 포트폴리오 API 테스트를 위한 최소 포트폴리오 데이터 생성
        Portfolio portfolio = Portfolio.create(
                title,
                "description",
                "client",
                "industry",
                null
        );
        return portfolioRepository.save(portfolio).getId();
    }

    private String issueToken() throws Exception {
        // 로그인 API를 호출해 응답 JSON에서 JWT 토큰을 추출한다.
        String response = mockMvc.perform(post("/api/admin/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username":"admin",
                                  "password":"admin1234!"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.path("data").path("accessToken").asText();
    }

    private <T> T newInstance(Class<T> type) {
        // protected 기본 생성자를 가진 엔티티를 리플렉션으로 생성한다.
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create instance: " + type.getName(), e);
        }
    }
}
