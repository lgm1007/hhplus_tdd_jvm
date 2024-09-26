# 동시성 제어 방식 분석
### 해당 프로젝트에서의 요구 사항
> 동시에 여러 요청이 들어오더라도 순서대로 혹은 한 번에 하나의 요청씩만 제어될 수 있도록 구현

### JVM 애플리케이션에서의 동시성 제어 방식
JVM(Java, Kotlin) 애플리케이션에서의 동시성 제어는 **여러 스레드가 동시에 접근할 때 발생할 수 있는 문제를 해결하기 위한 여러 가지 방법**이다.

#### 1. 내장 동기화
**synchronized 키워드**는 가장 기본적인 동기화 방법입니다. `synchronized` 키워드를 사용하여 특정 메서드 또는 블록을 동기화하면 해당 메서드 또는 블록이 실행되는 동안 다른 스레드가 접근할 수 없다.  
* `synchronized` 메서드 사용 예제
```kotlin
@Synchronized
fun synchronizedMethod() {
    // 메서드 코드 ...
}
```

* `synchronized` 블록 사용 예제
```kotlin
fun doSomethingBlock() {
    synchronized(this) {
        // 코드 ...
    }
}
```

#### 2. Lock 인터페이스
`java.util.concurrent.locks` 패키지 내 **Lock 인터페이스**는 다양한 Lock 구현을 제공합니다. 이를 통해 유연하게 동기화를 관리할 수 있다.  
* Lock 구현체의 대표적인 종류
  1. `ReentrantLock`: 재 진입이 가능한 Lock, `wait()`, `notify()`와 같이 락을 획득하고 해제하는 과정을 개발자가 직접 제어할 수 있다.
  2. `ReentrantReadWriteLock`: 한 쌍의 Lock, 읽기 전용 작업용 Lock과 쓰기용 Lock
  3. `StampedLock`: 낙관적, 읽기 전용, 쓰기 전용 모드와 같이 3가지 모드가 있는 Lock 
* `ReentrantLock` 사용 예제
```kotlin
val lock = ReentrantLock()

fun doSomething() {
    lock.lock()
    try {
        // 코드 ...
    } finally {
        lock.unlock()
    }
}
```

#### 3. Volatile 변수
* **volatile 키워드**를 사용하여 변수를 선언하면, 캐시된 값을 사용하는 것을 방지하고, 변수의 변경 사항이 즉시 다른 스레드에 반영되도록 하기 떄문에, 해당 변수의 값을 모든 스레드에서 최신 상태로 유지할 수 있다.
* `volatile` 사용 예제
```kotlin
@Volatile
val flag = false
```

#### 4. Atomic 클래스
* `java.util.concurrent.atomic` 패키지 내 **Atomic 클래스**는 단일 변수를 원자적으로 업데이트하도록 기능을 제공해준다. 
* CAS (Compare And Swap) 알고리즘을 사용하여 메인 메모리에 저장된 값과 캐시에 저장된 값을 비교하여 일치하는 경우에만 값을 사용하도록 하여 원자성을 보장한다.
* `Atomic` 클래스 사용 예제
```kotlin
val atomicInt = AtomicInt(0)
atomicInt.incrementAndGet()
```

#### 5. Concurrent Collections
* `java.util.concurrent` 패키지 내 **동시성 컬렉션**은 멀티스레드 환경에서 안전하게 데이터 구조를 사용할 수 있게 제공해준다.
* `Concurrent Collections` 대표 종류
  * `ConcurrentHashMap`
  * `CopyOnWriteArrayList`

#### 6. CompletableFuture
* **CompletableFuture**는 비동기 작업을 체이닝하고 결과를 기다리며, 예외 처리를 하게 해주는 클래스
* CompletableFuture 사용 예제
```kotlin
val executor = Executors.newFixedThreadPool(10)
CompletableFuture.supplyAsync({
    // 비동기 작업 ...
}, executor).thenAccept {
    // 결과 처리 ...
}
```

#### 7. Kotlin Coroutines
* Kotlin에서의 **Coroutines**은 스레드를 차단하지 않고도 비동기 처리를 간편하게 할 수 있게 해주는 기능
* Coroutines 사용 예제
```kotlin
GlobalScope.launch {
    val result = async {
        // 비동기 작업 ...
    }.await()
}
```

### 요구 사항에 부합하는 동시성 제어 방식 분석
#### 상황 정리
* 요구 사항에 충족하기 위해서는 포인트 충전과 사용에 대한 기능에 대해 멀티 스레드 환경에서의 동시성 요청에 대해 제어가 가능해야 한다.
* 이미 포인트를 저장하거나 업데이트하는 로직은 `HashMap<Long, UserPoint>` 타입의 데이터 구조를 사용하고 있고, 이를 변경할 수는 없다.
* 결국, `PointService`에서 구현할 포인트 충전과 사용 기능에서 동시성 처리를 해줘야 한다.

#### 시행 착오와 구현 방향
1. 처음에는 요청이 들어올 때 `ConcurrentLinkedDeque`와 같은 동시성 제어 컬렉션에 요청 데이터를 넣어주고 순차적으로 꺼내주면서 해야하나? 라고 생각했다.
   * 하지만 다시 생각해보니 특정 유저 아이디에 대해 포인트 충전 요청이 오면, 충전 완료하고 해당 유저의 포인트 정보를 응답으로 줘야하는데 그러지 못한다는 문제가 있었다.
2. 두 번째로는 포인트 충전/사용 기능에 대해 `synchronized` 키워드를 사용해 구현하는 방법을 생각했다.
   * 하지만 이는 모든 요청에 대해 지연이 걸리게 되며, 다른 유저의 충전/사용 요청으로 인해 나의 충전/사용 요청이 지연되는 문제가 발생하게 된다.
   * 따라서 동일한 유저의 요청에 대해서는 제어를, 다른 유저의 요청에 대해서는 동시에 진행되도록 하는 방법이 필요했다.
3. 최종적인 방법으로 `ConcurrentHashMap`과 `Locks`을 사용하는 방법으로, 동일한 유저의 요청에 대해서 Lock을 하는 방식으로 구현하였다.
   * `ConcurrentHashMap<Long, ReentrantLock>` 타입의 맵을 `PointService`의 private 필드로 정의했다.
     * 충전과 사용 요청은 동시에 수행하면 안 되기 때문에 동일한 맵에 `Lock`을 관리했다.
   * 충전/사용 기능에서 요청 들어온 `userId` 값이 맵에 없으면 새로운 `Lock`을 획득하도록 했다. 이 것으로 다른 유저의 요청에 대해서는 동시에 수행 가능하도록 했다. 

#### 동시성 제어 기능 검증
해당 포인트 충전/사용의 동시성 제어에 대해 검증할 케이스는 두 가지를 고려했다.  
1. 비동기 방식으로 서로 다른 사용자의 충전 요청을 여러 번 요청하고 난 후 각 유저의 잔여 포인트 양과 전체 충전/사용 내역 검증
2. 비동기 방식으로 같은 사용자의 충전/사용 요청을 순차적으로 요청하고 난 후 유저의 잔여 포인트 양이 순차적으로 요청했을 때 나올 수 있는 양인지 검증
