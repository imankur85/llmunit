package example.foodscanner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FoodScannerController {

    private final FoodScanner scanner;

    public FoodScannerController(FoodScanner scanner) {
        this.scanner = scanner;
    }

    @GetMapping("/suggest")
    public String suggest(@RequestParam String query) {
        return scanner.suggest(query);
    }
}