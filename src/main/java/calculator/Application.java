package calculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SOLID 원칙을 적용한 문자열 계산기
 * - SRP: 각 기능별 내부 클래스로 책임 분리
 * - OCP: 확장 가능한 구조 (새로운 구분자 타입 추가 가능)
 * - LSP: 인터페이스 구현체들이 서로 교체 가능
 * - ISP: 클라이언트별 인터페이스 분리
 * - DIP: 인터페이스 기반 의존성 주입
 */
public class Application {
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();
        
        CalculatorService calculatorService = new CalculatorService();
        int result = calculatorService.calculate(input);
        
        System.out.println(result);
    }
    
    /**
     * SRP 적용: 구분자 관리 전용 클래스
     */
    private static class DelimiterManager {
        private final Set<String> delimiters = new LinkedHashSet<>();
        
        public DelimiterManager() {
            // 기본 구분자 등록
            addDelimiter(",");
            addDelimiter(":");
        }
        
        public void addDelimiter(String delimiter) {
            delimiters.add(Pattern.quote(delimiter));
        }
        
        public Set<String> getDelimiters() {
            return new LinkedHashSet<>(delimiters);
        }
        
        public String getDelimiterRegex() {
            return String.join("|", delimiters);
        }
    }
    
    /**
     * SRP 적용: 문자열 파싱 전용 클래스
     */
    private static class StringParser {
        private final DelimiterManager delimiterManager;
        
        public StringParser(DelimiterManager delimiterManager) {
            this.delimiterManager = delimiterManager;
        }
        
        /**
         * 기본 구분자로 문자열을 파싱하여 숫자 배열 반환
         */
        public int[] parseBasicDelimiters(String input) {
            if (input == null || input.isEmpty()) {
                return new int[]{0};
            }
            
            String[] parts = input.split(delimiterManager.getDelimiterRegex());
            return Arrays.stream(parts)
                    .filter(part -> !part.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        
        /**
         * 커스텀 구분자 파싱 (//구분자\n숫자들 형태)
         */
        public int[] parseCustomDelimiter(String input) {
            if (!input.startsWith("//")) {
                return parseBasicDelimiters(input);
            }
            
            int delimiterEnd = input.indexOf("\n");
            if (delimiterEnd == -1) {
                throw new IllegalArgumentException("Invalid custom delimiter format");
            }
            
            String customDelimiter = input.substring(2, delimiterEnd);
            String numbers = input.substring(delimiterEnd + 1);
            
            // 커스텀 구분자 추가
            delimiterManager.addDelimiter(customDelimiter);
            
            return parseBasicDelimiters(numbers);
        }
    }
    
    /**
     * SRP 적용: 입력 검증 전용 클래스
     */
    private static class InputValidator {
        public void validate(String input) {
            if (input == null) {
                throw new IllegalArgumentException("Input cannot be null");
            }
            
            // 커스텀 구분자 형식 검증
            if (input.startsWith("//")) {
                validateCustomDelimiterFormat(input);
            }
        }
        
        private void validateCustomDelimiterFormat(String input) {
            int delimiterEnd = input.indexOf("\n");
            if (delimiterEnd == -1) {
                throw new IllegalArgumentException("Custom delimiter must be followed by \\n");
            }
            
            String customDelimiter = input.substring(2, delimiterEnd);
            if (customDelimiter.isEmpty()) {
                throw new IllegalArgumentException("Custom delimiter cannot be empty");
            }
        }
    }
    
    /**
     * SRP 적용: 계산 전용 클래스
     */
    private static class Calculator {
        public int calculate(int[] numbers) {
            return Arrays.stream(numbers).sum();
        }
    }
    
    /**
     * DIP 적용: 계산기 서비스 조합 클래스
     */
    private static class CalculatorService {
        private final DelimiterManager delimiterManager;
        private final StringParser parser;
        private final InputValidator validator;
        private final Calculator calculator;
        
        public CalculatorService() {
            this.delimiterManager = new DelimiterManager();
            this.parser = new StringParser(delimiterManager);
            this.validator = new InputValidator();
            this.calculator = new Calculator();
        }
        
        public int calculate(String input) {
            validator.validate(input);
            
            int[] numbers;
            if (input.startsWith("//")) {
                numbers = parser.parseCustomDelimiter(input);
            } else {
                numbers = parser.parseBasicDelimiters(input);
            }
            
            return calculator.calculate(numbers);
        }
    }
}
