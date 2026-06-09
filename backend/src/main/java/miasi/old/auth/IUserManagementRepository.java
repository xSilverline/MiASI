// NIE UWZGLĘDNIAMY W OFICJALNYM PROJEKCIE

package miasi.old.auth;

import miasi.backend.domains.authorization.Identity;

public interface IUserManagementRepository {
  void save(Identity identity);
}
