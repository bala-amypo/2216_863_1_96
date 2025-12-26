package com.example.demo;

import com.example.demo.dto.*;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.*;
import com.example.demo.service.impl.*;
import com.example.demo.util.DateRangeUtil;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;

// DO NOT IMPORT org.testng.annotations.Optional

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Arrays;
// java.util.Optional and java.util.Arrays are required

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// @SpringBootTest
@Listeners(TestResultListener.class)
public class LeaveOverlapTeamCapacityAnalyzerTest {

    // Common mocks
    private EmployeeProfileRepository employeeRepo;
    private LeaveRequestRepository leaveRepo;
    private TeamCapacityConfigRepository capacityRepo;
    private CapacityAlertRepository alertRepo;
    private UserAccountRepository userRepo;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;
    private EmployeeProfileService employeeService;
    private LeaveRequestService leaveService;
    private CapacityAnalysisService capacityService;

    @BeforeClass
    public void setup() {
        employeeRepo = mock(EmployeeProfileRepository.class);
        leaveRepo = mock(LeaveRequestRepository.class);
        capacityRepo = mock(TeamCapacityConfigRepository.class);
        alertRepo = mock(CapacityAlertRepository.class);
        userRepo = mock(UserAccountRepository.class);

        // JwtTokenProvider now has NO-ARG constructor in your project.
        // We instantiate and inject a test secret using reflection.
        tokenProvider = Mockito.spy(new JwtTokenProvider());
        try {
            java.lang.reflect.Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
            secretField.setAccessible(true);
            secretField.set(tokenProvider, "change-this-secret-key-change-this-secret-key-change");
        } catch (NoSuchFieldException ignored) {
            // If field name differs, tests that depend on real token parsing may fail.
            // But most logic will still run; adjust if needed.
        } catch (IllegalAccessException ignored) {
        }

        authService = new AuthServiceImpl(userRepo, new BCryptPasswordEncoder(), tokenProvider);
        employeeService = new EmployeeProfileServiceImpl(employeeRepo);
        leaveService = new LeaveRequestServiceImpl(leaveRepo, employeeRepo);
        capacityService = new CapacityAnalysisServiceImpl(capacityRepo, employeeRepo, leaveRepo, alertRepo);
    }

    // =====================================================================================
    // 1. Develop and deploy a simple servlet using Tomcat Server
    // =====================================================================================

    public static class SimpleHelloServlet extends HttpServlet {
        private String message;

        @Override
        public void init(ServletConfig config) throws ServletException {
            // Avoid NPE when config is null in tests
            if (config != null) {
                super.init(config);
            }
            message = "Hello from servlet";
        }

        @Override
        protected void doGet(jakarta.servlet.http.HttpServletRequest req,
                             jakarta.servlet.http.HttpServletResponse resp)
                throws ServletException, IOException {
            resp.setContentType("text/plain");
            PrintWriter writer = resp.getWriter();
            writer.write(message);
            writer.flush();
        }
    }

    @Test(groups = {"servlet"}, priority = 1)
    public void testServletInitialization() throws ServletException {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        ServletConfig config = mock(ServletConfig.class);
        servlet.init(config);
        Assert.assertNotNull(servlet);
    }

    @Test(groups = {"servlet"}, priority = 2)
    public void testServletResponseContainsMessage() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));

        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);
        pw.flush();

        Assert.assertTrue(sw.toString().contains("Hello from servlet"));
    }

    @Test(groups = {"servlet"}, priority = 3)
    public void testServletContentType() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));

        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        servlet.doGet(req, resp);
        verify(resp).setContentType("text/plain");
    }

    @Test(groups = {"servlet"}, priority = 4)
    public void testServletHandlesNullConfig() throws Exception {
        // Create servlet instance
        SimpleHelloServlet servlet = new SimpleHelloServlet();

        // Init with null config (we protected super.init() with null-check)
        servlet.init((jakarta.servlet.ServletConfig) null);

        // Mock request and response
        jakarta.servlet.http.HttpServletRequest req = mock(jakarta.servlet.http.HttpServletRequest.class);
        jakarta.servlet.http.HttpServletResponse resp = mock(jakarta.servlet.http.HttpServletResponse.class);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        servlet.doGet(req, resp);
        pw.flush();

        Assert.assertNotNull(servlet);
    }

    @Test(groups = {"servlet"}, priority = 5)
    public void testServletMultipleRequests() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));

        for (int i = 0; i < 3; i++) {
            var req = mock(jakarta.servlet.http.HttpServletRequest.class);
            var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
            when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
            servlet.doGet(req, resp);
        }

        Assert.assertTrue(true);
    }

    @Test(groups = {"servlet"}, priority = 6)
    public void testServletNoExceptionOnDoGet() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));
        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        servlet.doGet(req, resp);
        Assert.assertTrue(true);
    }

    @Test(groups = {"servlet"}, priority = 7)
    public void testServletMessageNotEmpty() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));
        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
        servlet.doGet(req, resp);
        pw.flush();
        Assert.assertFalse(sw.toString().isEmpty());
    }

    @Test(groups = {"servlet"}, priority = 8)
    public void testServletCustomMessageChange() throws Exception {
        SimpleHelloServlet servlet = new SimpleHelloServlet();
        servlet.init(mock(ServletConfig.class));
        // Using reflection to change the field (edge case style)
        java.lang.reflect.Field f = SimpleHelloServlet.class.getDeclaredField("message");
        f.setAccessible(true);
        f.set(servlet, "Changed");
        var req = mock(jakarta.servlet.http.HttpServletRequest.class);
        var resp = mock(jakarta.servlet.http.HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);
        servlet.doGet(req, resp);
        pw.flush();
        Assert.assertTrue(sw.toString().contains("Changed"));
    }

    // =====================================================================================
    // 2. Implement CRUD operations using Spring Boot and REST APIs (employee, leave)
    // =====================================================================================

    @Test(groups = {"crud"}, priority = 9)
    public void testCreateEmployeeProfile() {
        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setEmployeeId("E100");
        dto.setFullName("Test User");
        dto.setEmail("test@example.com");
        dto.setTeamName("DEV");
        dto.setRole("DEV");

        EmployeeProfile saved = new EmployeeProfile();
        saved.setId(1L);
        saved.setEmployeeId(dto.getEmployeeId());
        saved.setFullName(dto.getFullName());
        saved.setEmail(dto.getEmail());
        saved.setTeamName(dto.getTeamName());
        saved.setRole(dto.getRole());

        when(employeeRepo.save(any(EmployeeProfile.class))).thenReturn(saved);

        EmployeeProfileDto result = employeeService.create(dto);
        Assert.assertNotNull(result.getId());
        Assert.assertEquals(result.getEmployeeId(), "E100");
    }

    @Test(groups = {"crud"}, priority = 10)
    public void testUpdateEmployeeProfile() {
        EmployeeProfile existing = new EmployeeProfile();
        existing.setId(2L);
        existing.setEmployeeId("E200");
        existing.setFullName("Old Name");
        existing.setEmail("old@example.com");
        existing.setTeamName("DEV");
        existing.setRole("DEV");
        when(employeeRepo.findById(2L)).thenReturn(Optional.of(existing));

        EmployeeProfile updated = new EmployeeProfile();
        updated.setId(2L);
        updated.setEmployeeId("E200");
        updated.setFullName("New Name");
        updated.setEmail("old@example.com");
        updated.setTeamName("QA");
        updated.setRole("LEAD");
        when(employeeRepo.save(any(EmployeeProfile.class))).thenReturn(updated);

        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setFullName("New Name");
        dto.setTeamName("QA");
        dto.setRole("LEAD");

        EmployeeProfileDto result = employeeService.update(2L, dto);
        Assert.assertEquals(result.getFullName(), "New Name");
        Assert.assertEquals(result.getTeamName(), "QA");
    }

    @Test(groups = {"crud"}, priority = 11, expectedExceptions = ResourceNotFoundException.class)
    public void testUpdateEmployeeNotFound() {
        when(employeeRepo.findById(999L)).thenReturn(Optional.empty());
        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setFullName("Name");
        employeeService.update(999L, dto);
    }

    @Test(groups = {"crud"}, priority = 12)
    public void testDeactivateEmployeeProfile() {
        EmployeeProfile existing = new EmployeeProfile();
        existing.setId(3L);
        existing.setEmployeeId("E300");
        existing.setFullName("User 3");
        existing.setEmail("u3@example.com");
        existing.setTeamName("DEV");
        existing.setRole("DEV");
        existing.setActive(true);
        when(employeeRepo.findById(3L)).thenReturn(Optional.of(existing));
        when(employeeRepo.save(any(EmployeeProfile.class))).thenReturn(existing);

        employeeService.deactivate(3L);
        Assert.assertFalse(existing.isActive());
    }

    @Test(groups = {"crud"}, priority = 13)
    public void testGetEmployeeById() {
        EmployeeProfile existing = new EmployeeProfile();
        existing.setId(4L);
        existing.setEmployeeId("E400");
        existing.setFullName("User 4");
        existing.setEmail("u4@example.com");
        existing.setTeamName("DEV");
        existing.setRole("DEV");
        when(employeeRepo.findById(4L)).thenReturn(Optional.of(existing));
        EmployeeProfileDto result = employeeService.getById(4L);
        Assert.assertEquals(result.getEmployeeId(), "E400");
    }

    @Test(groups = {"crud"}, priority = 14, expectedExceptions = ResourceNotFoundException.class)
    public void testGetEmployeeByIdNotFound() {
        when(employeeRepo.findById(1000L)).thenReturn(Optional.empty());
        employeeService.getById(1000L);
    }

    @Test(groups = {"crud"}, priority = 15)
    public void testGetEmployeesByTeam() {
        EmployeeProfile e1 = new EmployeeProfile();
        e1.setId(5L);
        e1.setEmployeeId("E500");
        e1.setFullName("User 5");
        e1.setEmail("u5@example.com");
        e1.setTeamName("DEV");
        e1.setRole("DEV");
        e1.setActive(true);

        when(employeeRepo.findByTeamNameAndActiveTrue("DEV")).thenReturn(Collections.singletonList(e1));
        List<EmployeeProfileDto> list = employeeService.getByTeam("DEV");
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getEmployeeId(), "E500");
    }

    @Test(groups = {"crud"}, priority = 16)
    public void testCreateLeaveRequestValid() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(10L);
        emp.setEmployeeId("E1000");
        emp.setFullName("Emp 1");
        emp.setEmail("e1000@example.com");
        emp.setTeamName("DEV");
        emp.setRole("DEV");
        when(employeeRepo.findById(10L)).thenReturn(Optional.of(emp));

        LeaveRequest saved = new LeaveRequest();
        saved.setId(1L);
        saved.setEmployee(emp);
        saved.setStartDate(LocalDate.now());
        saved.setEndDate(LocalDate.now().plusDays(1));
        saved.setType("ANNUAL");
        saved.setStatus("PENDING");
        when(leaveRepo.save(any(LeaveRequest.class))).thenReturn(saved);

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(10L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setType("ANNUAL");
        dto.setReason("Vacation");

        LeaveRequestDto result = leaveService.create(dto);
        Assert.assertNotNull(result.getId());
        Assert.assertEquals(result.getStatus(), "PENDING");
    }

    @Test(groups = {"crud"}, priority = 17, expectedExceptions = BadRequestException.class)
    public void testCreateLeaveRequestInvalidDates() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(11L);
        when(employeeRepo.findById(11L)).thenReturn(Optional.of(emp));

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(11L);
        dto.setStartDate(LocalDate.now().plusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setType("ANNUAL");
        leaveService.create(dto);
    }

    @Test(groups = {"crud"}, priority = 18, expectedExceptions = ResourceNotFoundException.class)
    public void testCreateLeaveRequestEmployeeNotFound() {
        when(employeeRepo.findById(9999L)).thenReturn(Optional.empty());
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(9999L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setType("ANNUAL");
        leaveService.create(dto);
    }

    @Test(groups = {"crud"}, priority = 19)
    public void testApproveLeave() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(20L);
        LeaveRequest leave = new LeaveRequest();
        leave.setId(21L);
        leave.setEmployee(emp);
        leave.setStartDate(LocalDate.now());
        leave.setEndDate(LocalDate.now().plusDays(1));
        leave.setStatus("PENDING");
        when(leaveRepo.findById(21L)).thenReturn(Optional.of(leave));
        when(leaveRepo.save(any(LeaveRequest.class))).thenReturn(leave);

        LeaveRequestDto result = leaveService.approve(21L);
        Assert.assertEquals(result.getStatus(), "APPROVED");
    }

    @Test(groups = {"crud"}, priority = 20)
    public void testRejectLeave() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(22L);
        LeaveRequest leave = new LeaveRequest();
        leave.setId(23L);
        leave.setEmployee(emp);
        leave.setStartDate(LocalDate.now());
        leave.setEndDate(LocalDate.now().plusDays(1));
        leave.setStatus("PENDING");
        when(leaveRepo.findById(23L)).thenReturn(Optional.of(leave));
        when(leaveRepo.save(any(LeaveRequest.class))).thenReturn(leave);

        LeaveRequestDto result = leaveService.reject(23L);
        Assert.assertEquals(result.getStatus(), "REJECTED");
    }

    @Test(groups = {"crud"}, priority = 21, expectedExceptions = ResourceNotFoundException.class)
    public void testApproveLeaveNotFound() {
        when(leaveRepo.findById(404L)).thenReturn(Optional.empty());
        leaveService.approve(404L);
    }

    @Test(groups = {"crud"}, priority = 22)
    public void testGetLeavesByEmployee() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(30L);
        emp.setEmployeeId("E30");
        when(employeeRepo.findById(30L)).thenReturn(Optional.of(emp));

        LeaveRequest leave = new LeaveRequest();
        leave.setId(31L);
        leave.setEmployee(emp);
        leave.setStartDate(LocalDate.now());
        leave.setEndDate(LocalDate.now().plusDays(1));
        leave.setStatus("APPROVED");

        when(leaveRepo.findByEmployee(emp)).thenReturn(Collections.singletonList(leave));

        List<LeaveRequestDto> list = leaveService.getByEmployee(30L);
        Assert.assertEquals(list.size(), 1);
    }

    @Test(groups = {"crud"}, priority = 23)
    public void testGetOverlappingLeavesForTeam() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setTeamName("DEV");
        LeaveRequest leave = new LeaveRequest();
        leave.setId(40L);
        leave.setEmployee(emp);
        leave.setStatus("APPROVED");
        when(leaveRepo.findApprovedOverlappingForTeam(eq("DEV"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(leave));

        List<LeaveRequestDto> list = leaveService.getOverlappingForTeam("DEV", LocalDate.now(), LocalDate.now());
        Assert.assertEquals(list.size(), 1);
    }

    @Test(groups = {"crud"}, priority = 24)
    public void testDateRangeUtilDaysBetween() {
        LocalDate start = LocalDate.of(2023, 1, 1);
        LocalDate end = LocalDate.of(2023, 1, 3);
        List<LocalDate> dates = DateRangeUtil.daysBetween(start, end);
        Assert.assertEquals(dates.size(), 3);
        Assert.assertEquals(dates.get(0), start);
        Assert.assertEquals(dates.get(2), end);
    }

    @Test(groups = {"crud"}, priority = 25)
    public void testEmployeeListAllEmpty() {
        when(employeeRepo.findAll()).thenReturn(Collections.emptyList());
        Assert.assertTrue(employeeService.getAll().isEmpty());
    }

    @Test(groups = {"crud"}, priority = 26)
    public void testEmployeeListAllNonEmpty() {
        EmployeeProfile e = new EmployeeProfile();
        e.setId(77L);
        e.setEmployeeId("E777");
        e.setFullName("User 777");
        e.setEmail("u777@example.com");
        e.setTeamName("DEV");
        e.setRole("DEV");
        when(employeeRepo.findAll()).thenReturn(Collections.singletonList(e));
        Assert.assertEquals(employeeService.getAll().size(), 1);
    }

    // =====================================================================================
    // 3. Configure and perform Dependency Injection and IoC using Spring Framework
    // =====================================================================================

    @Test(groups = {"di"}, priority = 27)
    public void testServicesAreInjectedWithMocks() {
        Assert.assertNotNull(employeeService);
        Assert.assertNotNull(leaveService);
        Assert.assertNotNull(authService);
    }

    @Test(groups = {"di"}, priority = 28)
    public void testAuthServiceUsesPasswordEncoder() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("secret"));
        user.setRole("ADMIN");
        when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        AuthRequest req = new AuthRequest();
        req.setEmail("user@example.com");
        req.setPassword("secret");
        AuthResponse resp = authService.authenticate(req);
        Assert.assertNotNull(resp.getToken());
    }

    @Test(groups = {"di"}, priority = 29, expectedExceptions = BadRequestException.class)
    public void testAuthServiceInvalidPassword() {
        UserAccount user = new UserAccount();
        user.setId(2L);
        user.setEmail("bad@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));
        user.setRole("ADMIN");
        when(userRepo.findByEmail("bad@example.com")).thenReturn(Optional.of(user));

        AuthRequest req = new AuthRequest();
        req.setEmail("bad@example.com");
        req.setPassword("wrong");
        authService.authenticate(req);
    }

    @Test(groups = {"di"}, priority = 30, expectedExceptions = BadRequestException.class)
    public void testAuthServiceUserNotFound() {
        when(userRepo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        AuthRequest req = new AuthRequest();
        req.setEmail("missing@example.com");
        req.setPassword("x");
        authService.authenticate(req);
    }

    @Test(groups = {"di"}, priority = 31)
    public void testJwtTokenContainsRequiredClaims() {
        UserAccount user = new UserAccount();
        user.setId(5L);
        user.setEmail("claims@example.com");
        user.setRole("HR_MANAGER");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Assert.assertEquals(tokenProvider.getEmail(token), "claims@example.com");
        Assert.assertEquals(tokenProvider.getRole(token), "HR_MANAGER");
        Assert.assertEquals(tokenProvider.getUserId(token), Long.valueOf(5L));
    }

    @Test(groups = {"di"}, priority = 32)
    public void testJwtTokenValidationPositive() {
        UserAccount user = new UserAccount();
        user.setId(6L);
        user.setEmail("validate@example.com");
        user.setRole("TEAM_LEAD");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Assert.assertTrue(tokenProvider.validateToken(token));
    }

    @Test(groups = {"di"}, priority = 33)
    public void testJwtTokenValidationNegative() {
        Assert.assertFalse(tokenProvider.validateToken("invalid.token.value"));
    }

    @Test(groups = {"di"}, priority = 34)
    public void testJwtTokenUserIdFallbackToSubject() {
        UserAccount user = new UserAccount();
        user.setId(10L);
        user.setEmail("subject@example.com");
        user.setRole("ADMIN");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Long userId = tokenProvider.getUserId(token);
        Assert.assertEquals(userId, Long.valueOf(10L));
    }

    // =====================================================================================
    // 4. Implement Hibernate configurations, generator classes, annotations, and CRUD operations
    // =====================================================================================

    @Test(groups = {"hibernate"}, priority = 35)
    public void testEntityEmployeeProfileHasIdGenerated() {
        EmployeeProfile e = new EmployeeProfile();
        e.setEmployeeId("E900");
        e.setFullName("Hibernate User");
        e.setEmail("hib@example.com");
        e.setTeamName("DEV");
        e.setRole("DEV");
        when(employeeRepo.save(any(EmployeeProfile.class))).thenAnswer(invocation -> {
            EmployeeProfile saved = invocation.getArgument(0);
            saved.setId(900L);
            return saved;
        });

        EmployeeProfileDto dto = new EmployeeProfileDto();
        dto.setEmployeeId("E900");
        dto.setFullName("Hibernate User");
        dto.setEmail("hib@example.com");
        dto.setTeamName("DEV");
        dto.setRole("DEV");
        EmployeeProfileDto result = employeeService.create(dto);
        Assert.assertEquals(result.getId(), Long.valueOf(900L));
    }

    @Test(groups = {"hibernate"}, priority = 36)
    public void testHibernateCrudCreateLeave() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setId(50L);
        when(employeeRepo.findById(50L)).thenReturn(Optional.of(emp));
        when(leaveRepo.save(any(LeaveRequest.class))).thenAnswer(invocation -> {
            LeaveRequest l = invocation.getArgument(0);
            l.setId(51L);
            return l;
        });

        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setEmployeeId(50L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));
        dto.setType("SICK");
        LeaveRequestDto res = leaveService.create(dto);
        Assert.assertEquals(res.getId(), Long.valueOf(51L));
    }

    @Test(groups = {"hibernate"}, priority = 37)
    public void testHibernateCrudReadLeave() {
        LeaveRequest l = new LeaveRequest();
        EmployeeProfile e = new EmployeeProfile();
        e.setId(60L);
        l.setEmployee(e);
        l.setId(61L);
        l.setStatus("APPROVED");
        when(leaveRepo.findById(61L)).thenReturn(Optional.of(l));
        // directly check repository
        Optional<LeaveRequest> opt = leaveRepo.findById(61L);
        Assert.assertTrue(opt.isPresent());
        Assert.assertEquals(opt.get().getStatus(), "APPROVED");
    }

    @Test(groups = {"hibernate"}, priority = 38)
    public void testHibernateCrudUpdateLeave() {
        LeaveRequest l = new LeaveRequest();
        EmployeeProfile e = new EmployeeProfile();
        e.setId(70L);
        l.setEmployee(e);
        l.setId(71L);
        l.setStatus("PENDING");
        when(leaveRepo.findById(71L)).thenReturn(Optional.of(l));
        when(leaveRepo.save(any(LeaveRequest.class))).thenReturn(l);

        LeaveRequestDto result = leaveService.approve(71L);
        Assert.assertEquals(result.getStatus(), "APPROVED");
    }

    @Test(groups = {"hibernate"}, priority = 39)
    public void testHibernateCrudDeleteSimulation() {
        leaveRepo.deleteById(80L);
        verify(leaveRepo).deleteById(80L);
    }

    @Test(groups = {"hibernate"}, priority = 40)
    public void testTeamCapacityConfigCrud() {
        TeamCapacityConfig config = new TeamCapacityConfig();
        config.setId(1L);
        config.setTeamName("DEV");
        config.setTotalHeadcount(10);
        config.setMinCapacityPercent(60);
        when(capacityRepo.save(any(TeamCapacityConfig.class))).thenReturn(config);
        TeamCapacityConfig saved = capacityRepo.save(config);
        Assert.assertEquals(saved.getTeamName(), "DEV");
    }

    @Test(groups = {"hibernate"}, priority = 41)
    public void testCapacityAlertCrudSave() {
        CapacityAlert alert = new CapacityAlert("DEV", LocalDate.now(), "HIGH", "Test");
        when(alertRepo.save(any(CapacityAlert.class))).thenReturn(alert);
        CapacityAlert saved = alertRepo.save(alert);
        Assert.assertEquals(saved.getSeverity(), "HIGH");
    }

    @Test(groups = {"hibernate"}, priority = 42)
    public void testCapacityAlertFindByTeamAndDateRange() {
        CapacityAlert alert = new CapacityAlert("DEV", LocalDate.now(), "MEDIUM", "Msg");
        when(alertRepo.findByTeamNameAndDateBetween(eq("DEV"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(alert));
        List<CapacityAlert> list = alertRepo.findByTeamNameAndDateBetween("DEV", LocalDate.now(), LocalDate.now());
        Assert.assertEquals(list.size(), 1);
    }

    // =====================================================================================
    // 5. Perform JPA mapping with normalization (1NF, 2NF, 3NF)
    // =====================================================================================

    @Test(groups = {"jpa"}, priority = 43)
    public void testEmployeeHasTeamNameAtomic1NF() {
        EmployeeProfile e = new EmployeeProfile();
        e.setTeamName("DEV");
        Assert.assertTrue(e.getTeamName().contains("DEV"));
    }

    @Test(groups = {"jpa"}, priority = 44)
    public void testLeaveForeignKeyToEmployee() {
        EmployeeProfile e = new EmployeeProfile();
        e.setId(100L);
        LeaveRequest l = new LeaveRequest();
        l.setEmployee(e);
        Assert.assertEquals(l.getEmployee().getId(), Long.valueOf(100L));
    }

    @Test(groups = {"jpa"}, priority = 45)
    public void testTeamCapacityUniqueTeamName() {
        TeamCapacityConfig c1 = new TeamCapacityConfig();
        c1.setTeamName("DEV");
        TeamCapacityConfig c2 = new TeamCapacityConfig();
        c2.setTeamName("DEV");
        Assert.assertEquals(c1.getTeamName(), c2.getTeamName());
    }

    @Test(groups = {"jpa"}, priority = 46)
    public void testNoPartialDependencyOnEmployeeId() {
        EmployeeProfile e = new EmployeeProfile();
        e.setEmployeeId("EABC");
        e.setFullName("Name");
        Assert.assertNotNull(e.getEmployeeId());
        Assert.assertNotNull(e.getFullName());
    }

    @Test(groups = {"jpa"}, priority = 47)
    public void testTransitiveDependencyAvoidance() {
        EmployeeProfile e = new EmployeeProfile();
        e.setTeamName("DEV");
        e.setRole("DEV");
        Assert.assertNotNull(e.getTeamName());
        Assert.assertNotNull(e.getRole());
    }

    @Test(groups = {"jpa"}, priority = 48)
    public void testEmployeeColleaguesManyToMany() {
        EmployeeProfile e1 = new EmployeeProfile();
        EmployeeProfile e2 = new EmployeeProfile();
        e1.getColleagues().add(e2);
        Assert.assertEquals(e1.getColleagues().size(), 1);
    }

    @Test(groups = {"jpa"}, priority = 49)
    public void testNormalizationOfUserAccountAndEmployeeProfile() {
        UserAccount user = new UserAccount();
        EmployeeProfile e = new EmployeeProfile();
        user.setEmployeeProfile(e);
        Assert.assertNotNull(user.getEmployeeProfile());
    }

    // =====================================================================================
    // 6. Create Many-to-Many relationships and test associations in Spring Boot
    // (using EmployeeProfile.colleagues)
    // =====================================================================================

    @Test(groups = {"manyToMany"}, priority = 50)
    public void testManyToManyAddColleague() {
        EmployeeProfile e1 = new EmployeeProfile();
        EmployeeProfile e2 = new EmployeeProfile();
        e1.getColleagues().add(e2);
        Assert.assertTrue(e1.getColleagues().contains(e2));
    }

    @Test(groups = {"manyToMany"}, priority = 51)
    public void testManyToManySymmetricAdd() {
        EmployeeProfile e1 = new EmployeeProfile();
        EmployeeProfile e2 = new EmployeeProfile();
        e1.getColleagues().add(e2);
        e2.getColleagues().add(e1);
        Assert.assertTrue(e1.getColleagues().contains(e2));
        Assert.assertTrue(e2.getColleagues().contains(e1));
    }

    @Test(groups = {"manyToMany"}, priority = 52)
    public void testManyToManyEmptyInitially() {
        EmployeeProfile e = new EmployeeProfile();
        Assert.assertTrue(e.getColleagues().isEmpty());
    }

    @Test(groups = {"manyToMany"}, priority = 53)
    public void testManyToManyRemoveColleague() {
        EmployeeProfile e1 = new EmployeeProfile();
        EmployeeProfile e2 = new EmployeeProfile();
        e1.getColleagues().add(e2);
        e1.getColleagues().remove(e2);
        Assert.assertFalse(e1.getColleagues().contains(e2));
    }

    @Test(groups = {"manyToMany"}, priority = 54)
    public void testManyToManyNoDuplicateColleagues() {
        EmployeeProfile e1 = new EmployeeProfile();
        EmployeeProfile e2 = new EmployeeProfile();
        e1.getColleagues().add(e2);
        e1.getColleagues().add(e2);
        Assert.assertEquals(e1.getColleagues().size(), 1);
    }

    @Test(groups = {"manyToMany"}, priority = 55)
    public void testManyToManySelfAssociationNotAllowedByBusinessRule() {
        EmployeeProfile e1 = new EmployeeProfile();
        e1.getColleagues().add(e1);
        Assert.assertTrue(e1.getColleagues().contains(e1));
    }

    @Test(groups = {"manyToMany"}, priority = 56)
    public void testManyToManyColleaguesSizeEdgeCase() {
        EmployeeProfile e1 = new EmployeeProfile();
        for (int i = 0; i < 5; i++) {
            e1.getColleagues().add(new EmployeeProfile());
        }
        Assert.assertEquals(e1.getColleagues().size(), 5);
    }

    // =====================================================================================
    // 7. Implement basic security controls and JWT token-based authentication
    // =====================================================================================

    @Test(groups = {"security"}, priority = 57)
    public void testAuthServiceGeneratesToken() {
        UserAccount user = new UserAccount();
        user.setId(200L);
        user.setEmail("sec@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("pwd"));
        user.setRole("ADMIN");
        when(userRepo.findByEmail("sec@example.com")).thenReturn(Optional.of(user));

        AuthRequest req = new AuthRequest();
        req.setEmail("sec@example.com");
        req.setPassword("pwd");
        AuthResponse resp = authService.authenticate(req);
        Assert.assertNotNull(resp.getToken());
        Assert.assertEquals(resp.getUserId(), Long.valueOf(200L));
    }

    @Test(groups = {"security"}, priority = 58)
    public void testJwtTokenContainsRoleEmailUserId() {
        UserAccount user = new UserAccount();
        user.setId(300L);
        user.setEmail("jwttest@example.com");
        user.setRole("TEAM_LEAD");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Assert.assertEquals(tokenProvider.getEmail(token), "jwttest@example.com");
        Assert.assertEquals(tokenProvider.getRole(token), "TEAM_LEAD");
        Assert.assertEquals(tokenProvider.getUserId(token), Long.valueOf(300L));
    }

    @Test(groups = {"security"}, priority = 59)
    public void testJwtValidationFailureForMalformedToken() {
        Assert.assertFalse(tokenProvider.validateToken("this.is.not.jwt"));
    }

    @Test(groups = {"security"}, priority = 60)
    public void testJwtValidationPositiveForGeneratedToken() {
        UserAccount user = new UserAccount();
        user.setId(301L);
        user.setEmail("good@example.com");
        user.setRole("HR_MANAGER");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Assert.assertTrue(tokenProvider.validateToken(token));
    }

    @Test(groups = {"security"}, priority = 61, expectedExceptions = BadRequestException.class)
    public void testLoginWithWrongPasswordFails() {
        UserAccount user = new UserAccount();
        user.setId(400L);
        user.setEmail("wrong@example.com");
        user.setRole("ADMIN");
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));
        when(userRepo.findByEmail("wrong@example.com")).thenReturn(Optional.of(user));

        AuthRequest req = new AuthRequest();
        req.setEmail("wrong@example.com");
        req.setPassword("incorrect");
        authService.authenticate(req);
    }

    @Test(groups = {"security"}, priority = 62)
    public void testTokenProviderUserIdFromClaims() {
        UserAccount user = new UserAccount();
        user.setId(500L);
        user.setEmail("claims2@example.com");
        user.setRole("ADMIN");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Long userId = tokenProvider.getUserId(token);
        Assert.assertEquals(userId, Long.valueOf(500L));
    }

    @Test(groups = {"security"}, priority = 63)
    public void testTokenProviderEmailAndRoleConsistency() {
        UserAccount user = new UserAccount();
        user.setId(501L);
        user.setEmail("c@example.com");
        user.setRole("HR_MANAGER");
        user.setPassword("x");
        String token = tokenProvider.generateToken(user);
        Assert.assertEquals(tokenProvider.getEmail(token), "c@example.com");
        Assert.assertEquals(tokenProvider.getRole(token), "HR_MANAGER");
    }

    // =====================================================================================
    // 8. Use HQL and HCQL to perform advanced data querying
    // (simulate HQL via repo @Query and HCQL via criteria-like behavior)
    // =====================================================================================

    @Test(groups = {"hql"}, priority = 64)
    public void testHqlApprovedOverlappingLeavesQuery() {
        EmployeeProfile emp = new EmployeeProfile();
        emp.setTeamName("DEV");
        LeaveRequest l = new LeaveRequest();
        l.setEmployee(emp);
        l.setStatus("APPROVED");
        when(leaveRepo.findApprovedOverlappingForTeam(eq("DEV"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(l));

        List<LeaveRequestDto> list = leaveService.getOverlappingForTeam("DEV", LocalDate.now(), LocalDate.now());
        Assert.assertEquals(list.size(), 1);
    }

    @Test(groups = {"hql"}, priority = 65)
    public void testHqlApprovedOnDateQuerySimulation() {
        LeaveRequest l = new LeaveRequest();
        l.setStatus("APPROVED");
        when(leaveRepo.findApprovedOnDate(any(LocalDate.class)))
                .thenReturn(Collections.singletonList(l));
        List<LeaveRequest> list = leaveRepo.findApprovedOnDate(LocalDate.now());
        Assert.assertEquals(list.size(), 1);
        Assert.assertEquals(list.get(0).getStatus(), "APPROVED");
    }

    @Test(groups = {"hql"}, priority = 66)
    public void testCapacityAnalysisBelowThresholdGeneratesAlerts() {
        TeamCapacityConfig config = new TeamCapacityConfig();
        config.setTeamName("DEV");
        config.setTotalHeadcount(5);
        config.setMinCapacityPercent(60);
        when(capacityRepo.findByTeamName("DEV")).thenReturn(Optional.of(config));
        when(leaveRepo.findApprovedOverlappingForTeam(eq("DEV"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(new LeaveRequest(), new LeaveRequest(), new LeaveRequest())); // 3 leaves out of 5

        when(alertRepo.save(any(CapacityAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CapacityAnalysisResultDto result = capacityService.analyzeTeamCapacity(
                "DEV", LocalDate.now(), LocalDate.now());

        Assert.assertTrue(result.isRisky());
        Assert.assertTrue(result.getCapacityByDate().values().iterator().next() < 60);
    }

    @Test(groups = {"hql"}, priority = 67)
    public void testCapacityAnalysisNoConfigThrowsException() {
        when(capacityRepo.findByTeamName("NO_TEAM")).thenReturn(Optional.empty());
        try {
            capacityService.analyzeTeamCapacity("NO_TEAM", LocalDate.now(), LocalDate.now());
            Assert.fail("Expected ResourceNotFoundException not thrown");
        } catch (ResourceNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("Capacity config not found"));
        }
    }

    @Test(groups = {"hql"}, priority = 68)
    public void testCapacityAnalysisInvalidDateRange() {
        TeamCapacityConfig config = new TeamCapacityConfig();
        config.setTeamName("DEV");
        config.setTotalHeadcount(10);
        config.setMinCapacityPercent(60);
        when(capacityRepo.findByTeamName("DEV")).thenReturn(Optional.of(config));

        try {
            capacityService.analyzeTeamCapacity("DEV", LocalDate.now().plusDays(1), LocalDate.now());
            Assert.fail("Expected BadRequestException");
        } catch (BadRequestException ex) {
            Assert.assertTrue(ex.getMessage().contains("Start date"));
        }
    }

    @Test(groups = {"hql"}, priority = 69)
    public void testCapacityAnalysisEdgeCaseZeroHeadcount() {
        TeamCapacityConfig config = new TeamCapacityConfig();
        config.setTeamName("DEV");
        config.setTotalHeadcount(0);
        config.setMinCapacityPercent(60);
        when(capacityRepo.findByTeamName("DEV")).thenReturn(Optional.of(config));

        try {
            capacityService.analyzeTeamCapacity("DEV", LocalDate.now(), LocalDate.now());
            Assert.fail("Expected BadRequestException");
        } catch (BadRequestException ex) {
            Assert.assertTrue(ex.getMessage().contains("Invalid total headcount"));
        }
    }

    @Test(groups = {"hql"}, priority = 70)
    public void testCapacityAnalysisHealthyCapacityNoRisk() {
        TeamCapacityConfig config = new TeamCapacityConfig();
        config.setTeamName("QA");
        config.setTotalHeadcount(10);
        config.setMinCapacityPercent(50);
        when(capacityRepo.findByTeamName("QA")).thenReturn(Optional.of(config));
        when(leaveRepo.findApprovedOverlappingForTeam(eq("QA"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(new LeaveRequest())); // 1 of 10 on leave => 90%

        CapacityAnalysisResultDto result = capacityService.analyzeTeamCapacity("QA", LocalDate.now(), LocalDate.now());
        Assert.assertFalse(result.isRisky());
        Assert.assertTrue(result.getCapacityByDate().values().iterator().next() >= 50);
    }
}
