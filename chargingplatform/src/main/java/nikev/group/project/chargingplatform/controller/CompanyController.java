package nikev.group.project.chargingplatform.controller;

import nikev.group.project.chargingplatform.model.Company;
import nikev.group.project.chargingplatform.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    @GetMapping("/names")
    public List<String> getAllCompanyNames() {
        List<Company> companies = companyRepository.findAll();
        return companies.stream()
                .map(Company::getName)
                .collect(Collectors.toList());
    }
}