package svarcondrej.stream_rec.controller.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.notNullValue;

@WebMvcTest(StatusControllerImpl.class)
class StatusControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStatus_ShouldReturnOnlineMessage() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("online"))
                .andExpect(jsonPath("$.message").value("Stream Recorder Backend is running!"))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }
}