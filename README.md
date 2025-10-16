# java-calculator-precourse
## 기능 요구 사항
입력한 문자열에서 숫자를 추출하여 더하는 계산기 구현
## 구현할 기능 목록
### [요구사항 1]
쉼표, 콜론을 구분자로 가지는 문자열을 전달하는 경우 구분자를 기준으로 분리한 각 숫자의 합을 반환 
  - 예: "" => 0, "1,2" => 3, "1,2,3" => 6, "1,2:3" => 6
  - [풀이] 예에서 불수 있듯이 null은 0이다. 
  - [풀이] 문자열을 파싱해서 지정한 구분자를 인식하면 빼거나 공백으로 대체 -> 공백단위로 숫자 구분하여 배열에 넣고(arr[]) -> 각 배열의 합 구하기
  - [풀이] 구분자도 배열이나 자료구조에 등록하여 `읽어서` `쓰기`
  
### [요구사항 2]
앞의 기본구분자 외 커스텀 구분자 지정가능. 커스텀 구분자는 문자열 앞부분의 "//"와 "\n"사이에 위치하는 문자를 커스텀 구분자로 사용
- **사용자가 잘못된 값을 입력할 경우** `IllegalArgumentException`을 발생시킨 후 애플리케이션은 종료되어야 한다.


- [입력 요구사항] 구분자와 양수로 구성된 문자열
- [출력 요구사항] 덧셈 결과
- 
## 설계 
>  쉼표, 콜론을 어떻게 구분할 것인가? 
 
문자열을 파싱 `split()`메서드 사용

>  잘못된 값은 무엇인가?
  - 숫자가 포함되지 않은 문자열
  - 구분자나 커스텀 구분자로만 이루어진 문자열
  - 구분자나 커스텀 구분자로 이루어 지지 않은 문자열

### Used method (javadoc)
1. split()
-  주어진 정규 표현식과 일치하는 부분을 기준으로 이 문장을 분리
- 내부적으로 split(String regex, int limit)메서드를 limit 0을 주어 호출한 값과 동일하게 작동
- 결과 배열에는 문자열 끝에 생기는 빈 문자열 요소들이 포함되지 않음.
  - 예를 들어 "boo:and:foo"라는 문자열을 아래 정규식으로 분리한 결과는 다음과 같습니다. 
    - regex: : . result : {"boo", "and", "foo"}
  > parameter

    regex: 문자열 분리시 사용할 구분자 정규표현식

  > returns(반환값)

    주어진 정규표현식에 따라 분리된 **문자열 배열** 반환

## 구현
> 핵심: 구분자 컬렉션을 데이터로 관리하고, 해당 컬렉션을 기반으로 동적으로 문자열을 split하는 parser structure

[기능1 - 문자열을 다양한 구분자로 파싱한다.]

[기능 2 - 구분자를 자료구조에서 관리한다.]
- data strucuture
```java
private final Set<String> delimeter = new LinkedHashSet<>();
```
- delimeter 등록

```java
import com.sun.source.tree.PatternTree;

public void addDelimeter(String delimeter) {    //regex-safe하게 등록 (Pattern.quote 사용)
    set.add(Pattern.quote(delimeter));
}
```

- regex 빌드
```java
private String buildRegex(){
    if(delimeter.isEmpty()){
        throw new IllegalArgumentException();
    }
    return String.join("|",delimeter);
}
```

[기능 3- 새로운 구분자를 추가하거나 제거시 코드 수정 없이 동적으로 반영한다.]