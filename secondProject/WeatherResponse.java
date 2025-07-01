package basicProjectII.secondProject;

public class WeatherResponse {

    // 내부 클래스 Item
    public static class Item {
        private String baseDate; // 기준 날짜
        private String baseTime; // 기준 시간
        private String category; // 데이터 유형 (T1H, PTY 등)
        private String fcstDate; // 예보 날짜
        private String fcstTime; // 예보 시간
        private float obsrValue; // 초단기실황 관측 값
        private float fcstValue; // 단기예보, 초단기예보 관측 값
        private int nx; // X 좌표
        private int ny; // Y 좌표
        private String level1; // 상위 지역
        private String level2; // 하위 지역
        private String createdAt; // 검색 시간

        // Getter & Setter
        public String getBaseDate() {
            return baseDate;
        }

        public void setBaseDate(String baseDate) {
            this.baseDate = baseDate;
        }

        public String getBaseTime() {
            return baseTime;
        }

        public void setBaseTime(String baseTime) {
            this.baseTime = baseTime;
        }

        public String getFcstDate(){
            return fcstDate;
        }

        public void setFcstDate(String fcstDate){
            this.fcstDate = fcstDate;
        }

        public String getFcstTime(){
            return fcstTime;
        }

        public void setFcstTime(String fcstTime){
            this.fcstTime = fcstTime;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public float getObsrValue() {
            return obsrValue;
        }

        public void setObsrValue(float obsrValue) {
            this.obsrValue = obsrValue;
        }

        public float getFcstValue() {
            return fcstValue;
        }

        public void setFcstValue(float fcstValue) {
            this.fcstValue = fcstValue;
        }

        public int getNx() {
            return nx;
        }

        public void setNx(int nx) {
            this.nx = nx;
        }

        public int getNy() {
            return ny;
        }

        public void setNy(int ny) {
            this.ny = ny;
        }

        public String getLevel1() {
            return level1;
        }

        public void setLevel1(String level1) {
            this.level1 = level1;
        }

        public String getLevel2() {
            return level2;
        }

        public void setLevel2(String level2) {
            this.level2 = level2;
        }

        // createdAt Getter와 Setter 추가
        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            if (fcstDate != null && fcstTime != null) {
                // 단기예보, 초단기예보 데이터 포맷
                return "Item{" +
                        "category='" + category + '\'' +
                        ", fcstDate='" + fcstDate + '\'' +
                        ", fcstTime='" + fcstTime + '\'' +
                        ", fcstValue='" + fcstValue + '\''+
                        '}';
            } else {
                // 초단기실황 데이터 포맷
                return "Item{" +
                        "category='" + category + '\'' +
                        ", obsrValue='" + obsrValue + '\'' +
                        '}';
            }
        }

    }
}
