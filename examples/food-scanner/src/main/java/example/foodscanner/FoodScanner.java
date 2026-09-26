package example.foodscanner;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * The system under test: a small service that suggests healthy alternatives to a food query.
 */
@Service
public class FoodScanner {

    public String suggest(String query) {
        String q = query == null ? "" : query.toLowerCase();
        if (q.contains("cola")) {
            return "Try Zevia Zero Calorie Cola or Spindrift Lime Sparkling Water";
        }
        if (q.contains("chips")) {
            return "Try Popchips Sea Salt or Hippeas Cheese Dill";
        }
        return List.of("Sparkling water", "Oat milk").get(0);
    }
}