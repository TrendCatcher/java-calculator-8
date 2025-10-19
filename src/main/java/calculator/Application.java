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
 * 
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>기본 구분자(쉼표, 콜론)로 문자열 파싱</li>
 *   <li>커스텀 구분자 지원 (//구분자\n숫자들 형태)</li>
 *   <li>입력 검증 및 예외 처리</li>
 *   <li>숫자 합계 계산</li>
 * </ul>
 * 
 * <h3>SOLID 원칙 적용</h3>
 * <ul>
 *   <li><strong>SRP</strong>: 각 기능별 내부 클래스로 책임 분리</li>
 *   <li><strong>OCP</strong>: 확장 가능한 구조 (새로운 구분자 타입 추가 가능)</li>
 *   <li><strong>DIP</strong>: 의존성 주입을 통한 느슨한 결합</li>
 * </ul>
 * 
 * <h3>사용법</h3>
 * <pre>
 * // 일반 실행
 * java Application
 * 
 * // 테스트 실행
 * java Application test
 * </pre>
 * 
 * <h3>예제</h3>
 * <pre>
 * 입력: "1,2,3"     → 출력: 6
 * 입력: "1,2:3"     → 출력: 6
 * 입력: "//;\n1;2;3" → 출력: 6
 * 입력: ""          → 출력: 0
 * </pre>
 * 
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
            String[] parts = input.split(delimiterManager.getDelimiterRegex());
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
            int[] numbers = parser.parse(input);
            return calculator.calculate(numbers);
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
