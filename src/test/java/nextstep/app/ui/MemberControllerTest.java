package nextstep.app.ui;

import nextstep.app.SecurityConfig;
import nextstep.app.domain.Member;
import nextstep.app.domain.MemberRepository;
import nextstep.security.authentication.UsernamePasswordAuthenticationToken;
import nextstep.security.context.SecurityContext;
import nextstep.security.context.SecurityContextHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({MemberController.class, SecurityConfig.class})
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private MemberController memberController;

    @MockBean
    private MemberRepository memberRepository;

    private static final
    Member TEST_ADMIN_MEMBER = new Member("a@a.com", "a", "a", "hello.jpeg", Set.of("ADMIN"));

    @BeforeEach
    public void setup() {
        Mockito.when(
                memberController.list()
        ).thenReturn(ResponseEntity.ok(List.of(TEST_ADMIN_MEMBER)));

        Mockito.when(
                memberRepository.findByEmail(Mockito.anyString())
        ).thenReturn(Optional.of(TEST_ADMIN_MEMBER));

    }

    @Test
    public void testEndpoint() throws Exception {
        String token = Base64.getEncoder().encodeToString((TEST_ADMIN_MEMBER.getEmail() + ":" + TEST_ADMIN_MEMBER.getPassword()).getBytes());
        mockMvc.perform(
                        get("/members")
                                .header("Authorization", "Basic " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testSpringSecurityContext() throws Exception {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(TEST_ADMIN_MEMBER.getEmail(), TEST_ADMIN_MEMBER.getPassword(), TEST_ADMIN_MEMBER.getRoles())
        );
        SecurityContextHolder.setContext(context);

        mockMvc.perform(
                        get("/members")
                )
                .andExpect(status().isOk())
                .andDo(print());

    }
}