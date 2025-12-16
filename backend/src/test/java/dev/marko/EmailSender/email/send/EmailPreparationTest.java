package dev.marko.EmailSender.email.send;

import dev.marko.EmailSender.email.spintax.EmailPreparationService;
import dev.marko.EmailSender.email.spintax.SpintaxProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EmailPreparationTest {

    @Mock private SpintaxProcessor spinTextProcessor;

    @Test
    public void generationTest() {

        spinTextProcessor = new SpintaxProcessor();
        EmailPreparationService service = new EmailPreparationService(spinTextProcessor);

        String template = "Hi {{name}}, hope you are having a {great|wonderful|fantastic} day!";
        String result = service.generateMessageText(template, "Ana");
        String emptyName = service.generateMessageText(template, "");


        assertThat(result)
                .startsWith("Hi Ana, hope you are having a ")
                .contains(" day!");

        boolean validOption =
                result.contains("great") ||
                        result.contains("wonderful") ||
                        result.contains("fantastic");

        assertThat(validOption)
                .as("Result should contain one of the spintax options")
                .isTrue();

        assertThat(emptyName)
                .startsWith("Hi , hope you are having a ")
                .contains(" day!");
    }


}
