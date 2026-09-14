package org.kie.ks.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.kie.api.KieServices;
import org.kie.api.marshalling.Marshaller;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.KieSession;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionMarshalExtension implements KieServerExtension {
    private static final Logger log = LoggerFactory.getLogger(SessionMarshalExtension.class);
    public static final String EXTENSION_NAME = "SessionMarshal";
    private static final String SNAPSHOT_DIR = System.getProperty(
            "kie.session.snapshot.dir", "/tmp/kie-snapshots");

    private KieServerRegistry registry;
    private boolean initialized;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.registry = registry;
        initialized = true;
        log.info("{} extension initialized", EXTENSION_NAME);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
    }

    @Override
    public Integer getStartOrder() {
        return 20;
    }

    @Override
    public void createContainer(String id, KieContainerInstance container, Map<String, Object> parameters) {
        File snapshot = new File(SNAPSHOT_DIR, "snapshot-" + id + ".ser");
        if (!snapshot.exists()) {
            log.info("No snapshot for container {} at {}", id, snapshot);
            return;
        }

        String sessionName = "KBaseKS_stateful";
        CommandExecutor executor = registry.getKieSessionLookupManager()
                .lookup(sessionName, container, registry);
        if (!(executor instanceof KieSession)) {
            log.error("Could not obtain live session {} for {}", sessionName, id);
            return;
        }

        KieSession existingSession = (KieSession) executor;
        try (FileInputStream stream = new FileInputStream(snapshot)) {
            Marshaller marshaller = KieServices.get().getMarshallers()
                    .newMarshaller(existingSession.getKieBase());
            marshaller.unmarshall(stream, existingSession);
            log.info("Restored {} facts into {}", existingSession.getFactCount(), id);
        } catch (Exception e) {
            log.error("Restore failed for {}", id, e);
        }
    }

    @Override
    public void updateContainer(String id, KieContainerInstance container, Map<String, Object> parameters) {
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance container, Map<String, Object> parameters) {
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance container,
                                             Map<String, Object> parameters) {
        return true;
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        List<Object> components = new ArrayList<>();
        if (type == SupportedTransports.REST) {
            components.add(new MarshalRestResource(registry));
        }
        return components;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "BRM-MARSHAL";
    }

    @Override
    public List<Object> getServices() {
        return new ArrayList<>();
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    @Path("/server")
    public static class MarshalRestResource {
        private final KieServerRegistry registry;

        MarshalRestResource(KieServerRegistry registry) {
            this.registry = registry;
        }

        @GET
        @Path("/containers/{id}/marshal/{sessionName}")
        public Response marshal(@PathParam("id") String containerId,
                                @PathParam("sessionName") String sessionName) {
            try {
                KieContainerInstance container = registry.getContainer(containerId);
                if (container == null) {
                    return Response.status(404).entity("No such container: " + containerId).build();
                }

                CommandExecutor executor = registry.getKieSessionLookupManager()
                        .lookup(sessionName, container, registry);
                if (!(executor instanceof KieSession)) {
                    return Response.status(500).entity("Lookup did not return a KieSession").build();
                }

                KieSession session = (KieSession) executor;
                Marshaller marshaller = KieServices.get().getMarshallers()
                        .newMarshaller(session.getKieBase());
                File output = new File(SNAPSHOT_DIR, "snapshot-" + containerId + ".ser");
                output.getParentFile().mkdirs();
                try (FileOutputStream stream = new FileOutputStream(output)) {
                    marshaller.marshall(stream, session);
                }

                log.info("Marshalled container {} session {} - {} facts to {}",
                        containerId, sessionName, session.getFactCount(), output);
                return Response.ok("Marshalled " + session.getFactCount()
                        + " facts to " + output.getAbsolutePath()).build();
            } catch (Exception e) {
                log.error("Failed to marshal session", e);
                return Response.status(500).entity(e.getMessage()).build();
            }
        }
    }
}
