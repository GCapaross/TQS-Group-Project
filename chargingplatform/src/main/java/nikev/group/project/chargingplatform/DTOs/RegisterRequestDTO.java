package nikev.group.project.chargingplatform.DTOs;

public class RegisterRequestDTO {

    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private String accountType;

    public RegisterRequestDTO() {}

    public RegisterRequestDTO(
        String username,
        String password,
        String confirmPassword,
        String email,
        String accountType
    ) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.accountType = accountType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
}
