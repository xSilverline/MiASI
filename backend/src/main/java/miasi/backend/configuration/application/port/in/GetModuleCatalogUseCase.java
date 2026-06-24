package miasi.backend.configuration.application.port.in;

import miasi.backend.configuration.domain.model.Module;

import java.util.List;

public interface GetModuleCatalogUseCase {
  List<Module> getModuleCatalog();
}
