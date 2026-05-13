package com.mission.accesscontrol;
import java.util.Collection;
public interface IUserRepository {
    void save(Identity identity);
    Identity findByLogin(String login);
    Collection<Identity> findAll();
    boolean exists(String login);
}
