package com.fixflow;

import com.fixflow.service.DiagnosticoService;
import com.fixflow.service.ReparacionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityCorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DiagnosticoService diagnosticoService;

    @Autowired
    private ReparacionService reparacionService;

    @Test
    void shouldAllowPatchPreflightForSolicitudStateChanges() throws Exception {
        var result = mockMvc.perform(options("/api/solicitudes/1/estado")
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "PATCH")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(result.getResponse().getHeader("Access-Control-Allow-Methods"))
                .contains("PATCH");
    }

    @Test
    void shouldExposeDiagnosticoAndReparacionCollections() {
        assertThat(diagnosticoService.findAll()).isNotNull();
        assertThat(reparacionService.findAll()).isNotNull();
    }
}
