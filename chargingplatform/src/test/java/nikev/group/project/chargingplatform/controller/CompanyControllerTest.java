package nikev.group.project.chargingplatform.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import nikev.group.project.chargingplatform.security.JwtTokenProvider;
import java.util.List;

import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.repository.CompanyRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyRepository companyRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("GET /companies/names – retorna lista de nomes quando há empresas")
    void whenGetNames_thenReturnListOfNames() throws Exception {
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Nike");
        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Adidas");
        List<Company> companies = List.of(
                company1,
                company2
        );
        when(companyRepository.findAll()).thenReturn(companies);

        mockMvc.perform(get("/companies/names").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("Nike")))
                .andExpect(jsonPath("$[1]", is("Adidas")));
    }

    @Test
    @DisplayName("GET /companies/names – retorna lista vazia quando não há empresas")
    void whenGetNamesAndNoCompanies_thenReturnEmptyList() throws Exception {
        when(companyRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/companies/names").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /companies/names – retorna lista de nomes com nomes duplicados")
    void whenGetNamesWithDuplicateNames_thenReturnListWithDuplicates() throws Exception {
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Nike");
        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Nike");
        Company company3 = new Company();
        company3.setId(3L);
        company3.setName("Adidas");
        List<Company> companies = List.of(
                company1,
                company2,
                company3
        );
        when(companyRepository.findAll()).thenReturn(companies);

        mockMvc.perform(get("/companies/names").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", is("Nike")))
                .andExpect(jsonPath("$[1]", is("Nike")))
                .andExpect(jsonPath("$[2]", is("Adidas")));
    }
}