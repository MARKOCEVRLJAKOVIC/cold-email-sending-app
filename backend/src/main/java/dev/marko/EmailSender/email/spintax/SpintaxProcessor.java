package dev.marko.EmailSender.email.spintax;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SpintaxProcessor {

    // Uses a regex pattern to process spintax expressions inside { }, splitting options by |
    // Example: "Good {morning|evening|night}" → randomly selects one option.
    private static final Pattern SPINTAX_PATTERN = Pattern.compile("\\{([^{}]+?)\\}");
    private final Random random = new Random();

    public String process(String text) {
        Matcher matcher = SPINTAX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String[] options = matcher.group(1).split("\\|");
            String replacement = options[random.nextInt(options.length)].trim();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

}
