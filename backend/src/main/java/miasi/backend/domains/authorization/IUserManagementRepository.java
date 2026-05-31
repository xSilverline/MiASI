package miasi.backend.domains.authorization;

public interface IUserManagementRepository {
  void save(Identity identity);
}
