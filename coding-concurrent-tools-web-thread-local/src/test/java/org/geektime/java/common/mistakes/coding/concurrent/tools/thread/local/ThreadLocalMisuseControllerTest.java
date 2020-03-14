package org.geektime.java.common.mistakes.coding.concurrent.tools.thread.local;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@Slf4j
public class ThreadLocalMisuseControllerTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void wrong() throws Exception {
        mvc.perform(
                get("/thread-local/wrong")
                        .queryParam("userId", "1")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                matchAll(
                        status().isOk(),
                        jsonPath(".before").value("main:null"),
                        jsonPath(".after").value("main:1")
                )
        );
        mvc.perform(
                get("/thread-local/wrong")
                        .queryParam("userId", "2")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                matchAll(
                        status().isOk(),
                        jsonPath(".before").value("main:null"),
                        jsonPath(".after").value("main:2")
                )
        );
    }

    @Test
    public void right() throws Exception {
        String rightFirst = mvc.perform(
                get("/thread-local/right")
                        .queryParam("userId", "1")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                matchAll(
                        status().isOk(),
                        jsonPath(".before").value("main:null"),
                        jsonPath(".after").value("main:1")
                )
        ).andReturn().getResponse().getContentAsString();
        log.info("请求\"/thread-local/right?userId=1\"响应正确：{}", rightFirst);
        String rightSecond = mvc.perform(
                get("/thread-local/right")
                        .queryParam("userId", "2")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpect(
                matchAll(
                        status().isOk(),
                        jsonPath(".before").value("main:null"),
                        jsonPath(".after").value("main:2")
                )
        ).andReturn().getResponse().getContentAsString();
        log.info("请求\"/thread-local/right?userId=2\"响应正确，：{}", rightSecond);
    }
}