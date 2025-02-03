package no.nav.openapi.spec.utils.openapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath(DummyApplication.API_URI)
public class DummyApplication extends Application {
    public static final String API_URI = "/api";

}
