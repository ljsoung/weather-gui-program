# 🌦 Java GUI 기반 날씨 조회 애플리케이션

## 📌 프로젝트 개요
- **공공 데이터 포털 API**를 활용하여 GUI 기반 날씨 조회 애플리케이션 개발
- Java의 `Swing`을 사용하여 GUI를 구성하고, API에서 **실시간 날씨 데이터를 가져옴**
- **날씨 데이터를 시각적으로 표현 (아이콘 포함)** 및 사용자 편의성 강화

## 📜 주요 기능
✅ **사용자 입력을 통해 특정 지역의 날씨 정보를 조회**  
✅ **API 연동 (기상청 or OpenWeather API 활용)**  
✅ **GUI 화면에 온도, 날씨 상태(맑음, 흐림 등) 아이콘 표시**  
✅ **예외 처리 (네트워크 오류, 잘못된 입력 방지)**  
✅ **DB(MySQL) 활용하여 검색 기록 및 지역 데이터 관리**  
✅ **Socket 프로그래밍을 사용하여 클라이언트와 서버 간의 실시간 데이터 요청 및 응답 처리**  

---

## 🛠️ 기술 스택
| **구분**  | **기술 스택** |
|-----------|--------------|
| **언어** | Java |
| **GUI** | Swing, JFreeChart |
| **API 연동** | REST API (HttpURLConnection), Gson |
| **DB** | MySQL (검색 기록 저장) |
| **네트워크** | Socket 프로그래밍 (클라이언트-서버 통신) |
| **IDE** | IntelliJ IDEA |

---

## 🔧 실행 방법
1️⃣ **MySQL DB 설정**
- MySQL을 설치하고 실행합니다.
- DB ID를 'root'로 설정합니다.
- DB PASSWORD를 '0000'으로 설정합니다.

2️⃣ **SQL 파일 실행**
- 업로드 된 `.sql` 파일을 실행하여 테이블을 생성하고, 초기 데이터를 삽입합니다.

3️⃣ **프로그램 실행**
- IntelliJ 또는 Eclipse에서 Server 클래스의 `main()`을 실행하고 Client 클래스의 'main()'를 실행하세요.

## 📸 실행 화면
![image](https://github.com/user-attachments/assets/b6daadaf-c4b7-4ac7-b8f7-401e019b434a)
![image](https://github.com/user-attachments/assets/795253b3-aa20-4ade-b874-10d2d16a1431)
![image](https://github.com/user-attachments/assets/79eceb83-215d-43b8-ae0c-c8f6cce015bc)
