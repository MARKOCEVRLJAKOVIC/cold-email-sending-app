package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.send.batch.CsvParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CsvParserServiceTest {

    @InjectMocks
    CsvParserService csvParserService;


    @Test
    void parseCsv_ShouldParseCsvAndReturnRecipientDtoList(){

        var file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                ("name,email\n" +
                        "John Johnson,john@example.com\n" +
                        ",sara@example.com")
                        .getBytes(StandardCharsets.UTF_8)
        );


        var result = csvParserService.parseCsv(file);

        assertEquals(2, result.size());
        assertEquals("John",result.getFirst().getName());
        assertEquals("john@example.com", result.getFirst().getEmail());
        assertEquals("", result.get(1).getName());

    }

}
