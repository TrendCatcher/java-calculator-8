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
        // 테스트 모드 확인
        if (args.length > 0 && "test".equals(args[0])) {
            CalculatorTest.runAllTests();
            return;
        }
        
        // 일반 실행 모드
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String input = br.readLine();
        
        CalculatorService calculatorService = new CalculatorService();
        int result = calculatorService.calculate(input);
        
        System.out.println(result);
    }
    
    /**
     * ISP 적용: 구분자 전략 인터페이스
     */
    private interface DelimiterStrategy {
        Set<String> getDelimiters();
        String getDelimiterRegex();
        void addDelimiter(String delimiter);
    }
    
    /**
     * ISP 적용: 파싱 전략 인터페이스
     */
    private interface ParserStrategy {
        int[] parse(String input);
    }
    
    /**
     * ISP 적용: 검증 전략 인터페이스
     */
    private interface ValidationStrategy {
        void validate(String input);
    }
    
    /**
     * ISP 적용: 계산 전략 인터페이스
     */
    private interface CalculationStrategy {
        int calculate(int[] numbers);
    }
    
    /**
     * SRP 적용: 구분자 관리 전용 클래스 (LSP 적용)
     */
    private static class DelimiterManager implements DelimiterStrategy {
        private final Set<String> delimiters = new LinkedHashSet<>();
        
        public DelimiterManager() {
            // 기본 구분자 등록
            addDelimiter(",");
            addDelimiter(":");
        }
        
        @Override
        public void addDelimiter(String delimiter) {
            delimiters.add(Pattern.quote(delimiter));
        }
        
        @Override
        public Set<String> getDelimiters() {
            return new LinkedHashSet<>(delimiters);
        }
        
        @Override
        public String getDelimiterRegex() {
            return String.join("|", delimiters);
        }
    }
    
    /**
     * SRP 적용: 문자열 파싱 전용 클래스 (LSP 적용)
     */
    private static class StringParser implements ParserStrategy {
        private final DelimiterStrategy delimiterStrategy;
        
        public StringParser(DelimiterStrategy delimiterStrategy) {
            this.delimiterStrategy = delimiterStrategy;
        }
        
        @Override
        public int[] parse(String input) {
            if (input == null || input.isEmpty()) {
                return new int[]{0};
            }
            
            if (input.startsWith("//")) {
                return parseCustomDelimiter(input);
            } else {
                return parseBasicDelimiters(input);
            }
        }
        
        /**
         * 기본 구분자로 문자열을 파싱하여 숫자 배열 반환
         */
        private int[] parseBasicDelimiters(String input) {
            String[] parts = input.split(delimiterStrategy.getDelimiterRegex());
            return Arrays.stream(parts)
                    .filter(part -> !part.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
        }
        
        /**
         * 커스텀 구분자 파싱 (//구분자\n숫자들 형태)
         */
        private int[] parseCustomDelimiter(String input) {
            int delimiterEnd = input.indexOf("\n");
            if (delimiterEnd == -1) {
                throw new IllegalArgumentException("Invalid custom delimiter format");
            }
            
            String customDelimiter = input.substring(2, delimiterEnd);
            String numbers = input.substring(delimiterEnd + 1);
            
            // 커스텀 구분자 추가
            delimiterStrategy.addDelimiter(customDelimiter);
            
            return parseBasicDelimiters(numbers);
        }
    }
    
    /**
     * SRP 적용: 입력 검증 전용 클래스 (LSP 적용)
     */
    private static class InputValidator implements ValidationStrategy {
        @Override
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
     * SRP 적용: 계산 전용 클래스 (LSP 적용)
     */
    private static class Calculator implements CalculationStrategy {
        @Override
        public int calculate(int[] numbers) {
            return Arrays.stream(numbers).sum();
        }
    }
    
    /**
     * DIP 적용: 계산기 서비스 조합 클래스 (전략 패턴 적용)
     */
    private static class CalculatorService {
        private final DelimiterStrategy delimiterStrategy;
        private final ParserStrategy parserStrategy;
        private final ValidationStrategy validationStrategy;
        private final CalculationStrategy calculationStrategy;
        
        public CalculatorService() {
            this.delimiterStrategy = new DelimiterManager();
            this.parserStrategy = new StringParser(delimiterStrategy);
            this.validationStrategy = new InputValidator();
            this.calculationStrategy = new Calculator();
        }
        
        public int calculate(String input) {
            validationStrategy.validate(input);
            int[] numbers = parserStrategy.parse(input);
            return calculationStrategy.calculate(numbers);
        }
    }
    
    /**
     * 단위 테스트를 위한 테스트 메서드들
     */
    private static class CalculatorTest {
        
        public static void runAllTests() {
            System.out.println("=== 계산기 단위 테스트 시작 ===");
            
            testEmptyString();
            testBasicDelimiters();
            testCustomDelimiter();
            testInvalidInput();
            testComplexCases();
            
            System.out.println("=== 모든 테스트 통과! ===");
        }
        
        private static void testEmptyString() {
            CalculatorService service = new CalculatorService();
            int result = service.calculate("");
            assert result == 0 : "빈 문자열은 0을 반환해야 함";
            System.out.println("✓ 빈 문자열 테스트 통과");
        }
        
        private static void testBasicDelimiters() {
            CalculatorService service = new CalculatorService();
            
            int result1 = service.calculate("1,2");
            assert result1 == 3 : "1,2는 3이어야 함";
            
            int result2 = service.calculate("1,2,3");
            assert result2 == 6 : "1,2,3은 6이어야 함";
            
            int result3 = service.calculate("1,2:3");
            assert result3 == 6 : "1,2:3은 6이어야 함";
            
            System.out.println("✓ 기본 구분자 테스트 통과");
        }
        
        private static void testCustomDelimiter() {
            CalculatorService service = new CalculatorService();
            
            int result = service.calculate("//;\n1;2;3");
            assert result == 6 : "//;\n1;2;3은 6이어야 함";
            
            System.out.println("✓ 커스텀 구분자 테스트 통과");
        }
        
        private static void testInvalidInput() {
            CalculatorService service = new CalculatorService();
            
            try {
                service.calculate("//\n1,2,3");
                assert false : "빈 커스텀 구분자는 예외를 발생시켜야 함";
            } catch (IllegalArgumentException e) {
                System.out.println("✓ 잘못된 커스텀 구분자 예외 처리 테스트 통과");
            }
            
            try {
                service.calculate("//;\n1,2,3");
                assert false : "잘못된 구분자 사용은 예외를 발생시켜야 함";
            } catch (IllegalArgumentException e) {
                System.out.println("✓ 잘못된 구분자 사용 예외 처리 테스트 통과");
            }
        }
        
        private static void testComplexCases() {
            CalculatorService service = new CalculatorService();
            
            // 복합 구분자 테스트
            int result1 = service.calculate("//|\n1|2|3");
            assert result1 == 6 : "//|\n1|2|3은 6이어야 함";
            
            // 기본 구분자와 커스텀 구분자 혼합
            int result2 = service.calculate("//@\n1@2,3:4");
            assert result2 == 10 : "//@\n1@2,3:4는 10이어야 함";
            
            System.out.println("✓ 복합 케이스 테스트 통과");
        }
    }
}
