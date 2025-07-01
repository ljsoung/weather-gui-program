package basicProjectII.secondProject;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class ClientGui extends JFrame {
    public CardLayout cardLayout;
    public JPanel cardPanel;
    public SelectLocalPanel selectLocalPanel;
    public ProvideWeatherPanel provideWeatherPanel;
    public SearchHistoryPanel searchHistoryPanel;
    public SlidingMenuPanel slidingMenuPanel;

    public ClientGui(Client client) {
        setTitle("프로젝트2_임지성");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 600);
        setResizable(false);
        setLocationRelativeTo(null); // 프레임을 화면 중앙에 배치

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        selectLocalPanel = new SelectLocalPanel(client, this);
        provideWeatherPanel = new ProvideWeatherPanel(this);
        searchHistoryPanel = new SearchHistoryPanel(this);
        slidingMenuPanel = new SlidingMenuPanel(this, client);

        //화면 전환용 cardPanel
        cardPanel.add(selectLocalPanel, "SelectLocalPanel");
        cardPanel.add(provideWeatherPanel, "ProvideWeatherPanel");
        cardPanel.add(searchHistoryPanel, "SearchHistoryPanel");

        add(cardPanel);
        cardLayout.show(cardPanel, "SelectLocalPanel"); // 첫 화면: SelectLocalPanel

        setVisible(true);
    }

    // 날씨 제공 패널 표시
    public void showProvideWeatherPanel() {
        cardLayout.show(cardPanel, "ProvideWeatherPanel");
    }

    // 검색 기록 패널 표시
    public void showSearchHistoryPanel() {
        cardLayout.show(cardPanel, "SearchHistoryPanel");
    }
}


class SelectLocalPanel extends JPanel {

    //SelectLocalPanel에 올라갈 컴포넌트들
    private JLabel label = new JLabel("실시간 날씨 제공 시스템");
    private JComboBox<String> level1ComboBox = new JComboBox<>();
    private JComboBox<String> level2ComboBox = new JComboBox<>();
    private JButton inquiryButton = new JButton("날씨 조회");
    private JButton exitButton = new JButton("프로그램 종료");

    Client client; // 클라이언트 객체
    ClientGui clientGui; // GUI 객체

    public SelectLocalPanel(Client client, ClientGui clientGui) {
        this.client = client;
        this.clientGui = clientGui;
        setLayout(null);
        setBackground(Color.WHITE);

        // JLabel 설정
        label.setBounds(50, 50, 300, 50); // (x, y, width, height)
        label.setFont(new Font("굴림", Font.BOLD, 25));

        // 첫 번째 JCheckBox 설정
        level1ComboBox.setBounds(100, 150, 200, 40);
        level1ComboBox.addItem("지역을 선택해주세요"); // 초기 값 추가

        // 데이터베이스에서 level1 데이터를 가져와 JComboBox에 추가
        List<String> level1Data = DatabaseConnectionManager.getLevel1Data();
        for (String data : level1Data) {
            level1ComboBox.addItem(data); // 가져온 데이터 추가
        }

        // 두 번째 JCheckBox 설정
        level2ComboBox.setBounds(100, 220, 200, 40);
        level2ComboBox.addItem("지역을 선택해주세요"); // 초기 값 추가

        // level1ComboBox 액션 리스너 추가
        level1ComboBox.addActionListener(e -> {
            String selectedLevel1 = (String) level1ComboBox.getSelectedItem();
            if (!"지역을 선택해주세요".equals(selectedLevel1)) {
                // 선택된 level1에 따른 level2 데이터 가져오기
                List<String> level2Data = DatabaseConnectionManager.getLevel2Data(selectedLevel1);

                // level2ComboBox 초기화 후 새 데이터 추가
                level2ComboBox.removeAllItems();
                level2ComboBox.addItem("세부 지역을 선택해주세요");
                for (String data : level2Data) {
                    level2ComboBox.addItem(data);
                }
            }
        });


        // 조회 버튼 설정
        inquiryButton.setBounds(100, 290, 200, 40);

        // 조회 버튼 리스너
        inquiryButton.setBounds(100, 290, 200, 40);
        inquiryButton.addActionListener(e -> {
            String selectedLevel1 = (String) level1ComboBox.getSelectedItem();
            String selectedLevel2 = (String) level2ComboBox.getSelectedItem();

            if (!"지역을 선택해주세요".equals(selectedLevel1) &&
                    !"세부 지역을 선택해주세요".equals(selectedLevel2)) {
                // 비동기적으로 데이터 전송 및 결과 처리
                new Thread(() -> {
                    //좌표 저장을 위한 문자열 배열
                    String[] coordinates = DatabaseConnectionManager.getCoordinates(selectedLevel1, selectedLevel2);

                    if (coordinates != null) {
                        String message = coordinates[0] + "," + coordinates[1]; // 좌표 형식 예) : "55,127"
                        client.sendMessage(message); // 서버로 좌표 전송

                        // 지역 이름을 설정하여 GUI에 표시
                        String fullLocationName = selectedLevel1 + " " + selectedLevel2;
                        SwingUtilities.invokeLater(() -> {
                            clientGui.provideWeatherPanel.setLocationName(fullLocationName);
                            clientGui.showProvideWeatherPanel();
                        });
                    } else { // 좌표를 못찾을 시
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(this, "좌표를 찾을 수 없습니다.", "알림", JOptionPane.WARNING_MESSAGE)
                        );
                    }

                    // 로딩 패널에서 결과 패널로 전환 (GUI 스레드에서 실행)
                    SwingUtilities.invokeLater(() -> clientGui.showProvideWeatherPanel());
                }).start();
            } else { // 지역, 세부지역을 모두 선택하지 않았을 시 실행
                JOptionPane.showMessageDialog(this, "지역과 세부 지역을 모두 선택해주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 종료 버튼 설정
        exitButton.setBounds(100, 360, 200, 40);

        // 종료 버튼 액션 리스너 추가
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "프로그램을 종료하시겠습니까?", "종료 확인", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        // 패널에 컴포넌트 추가
        add(label);
        add(level1ComboBox);
        add(level2ComboBox);
        add(inquiryButton);
        add(exitButton);
    }
}

class ProvideWeatherPanel extends JPanel {

    private ClientGui clientGui; // ClientGui 객체 참조

    //초단기현황에 올라갈 컴포넌트들
    private JLabel locationLabel; // 지역 이름을 표시할 JLabel
    private JLabel weatherInfoLabel; // 날씨 정보를 표시할 JLabel
    private JLabel iconLabel; // 날씨 아이콘을 표시할 JLabel
    private JLabel temperatureLabel; // 현재 기온을 표시할 JLabel
    private JLabel windSpeedLabel; // 풍속
    private JLabel humidityLabel; // 습도
    private JButton backButton;
    private JButton menuButton;

    //아이콘 이미지들
    private ImageIcon sunnyIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/Sunny.png");
    private ImageIcon rainIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/Rain.png");
    private ImageIcon snowIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/Snow.png");
    private ImageIcon cloudMoonIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/CloudMoon.png");
    private ImageIcon loadingIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/LoadingIcon.png");
    private ImageIcon backIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/backIcon.png");
    private ImageIcon backRolloverIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/backRolloverIcon.png");
    private ImageIcon menuIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/MenuIcon.png");
    private ImageIcon menuRolloverIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/MenuRolloverIcon.png");

    //Panel들
    private JPanel northPanel = new JPanel(null); // 초단기실황 패널
    private JPanel centerPanel = new JPanel(); // 초단기예보 패널
    private JScrollPane scrollPane; // 가로 스크롤을 위한 JScrollPane
    private JPanel southPanel = new JPanel(); // 단기예보 패널
    private SlidingMenuPanel slidingMenuPanel; // 메뉴 패널 선언

    public ProvideWeatherPanel(ClientGui clientGui) {
        this.clientGui = clientGui; // ClientGui 객체 전달
        setLayout(new BorderLayout());
        northPanel.setBackground(Color.WHITE);
        centerPanel.setBackground(Color.WHITE);
        southPanel.setBackground(Color.WHITE);

        // slidingMenuPanel 초기화
        slidingMenuPanel = new SlidingMenuPanel(clientGui, clientGui.selectLocalPanel.client);


        // northPanel에 크기 설정
        northPanel.setPreferredSize(new Dimension(400, 280)); // 패널 크기 설정

        //backButton 구현
        backButton = new JButton("", backIcon);
        backButton.setBounds(20, 10, 48, 48);
        backButton.setBorderPainted(false); // 버튼 외곽선 제거
        backButton.setContentAreaFilled(false); // 내용영역 채우기 x
        backButton.setFocusPainted(false); // 버튼 선택 시 생기는 테두리 제거
        backButton.setRolloverIcon(backRolloverIcon);

        // 뒤로 가기 버튼 액션 리스너
        backButton.addActionListener(e -> {
            resetWeatherData(); // 날씨 데이터 초기화
            clientGui.cardLayout.show(clientGui.cardPanel, "SelectLocalPanel"); // 패널 전환
        });

        add(slidingMenuPanel); // 메뉴 패널 추가

        //menuButton 구현
        menuButton = new JButton("", menuIcon);
        menuButton.setBounds(320, 10, 48, 48);
        menuButton.setBorderPainted(false); // 버튼 테두리 제거
        menuButton.setContentAreaFilled(false); // 버튼 배경 투명화
        menuButton.setFocusPainted(false); // 포커스 테두리 제거
        menuButton.setOpaque(false); // 완전히 투명하게 설정
        menuButton.setRolloverIcon(menuRolloverIcon); // 마우스 오버 아이콘 설정


        // 메뉴 버튼 액션 리스너
        menuButton.addActionListener(e -> slidingMenuPanel.toggleMenu());

        // 컴포넌트 초기화 및 위치 설정
        locationLabel = new JLabel("지역 이름", SwingConstants.CENTER);
        locationLabel.setFont(new Font("굴림", Font.BOLD, 20));
        locationLabel.setBounds(50, 70, 300, 40); // x, y, width, height

        //날씨 아이콘 라벨
        iconLabel = new JLabel(loadingIcon, SwingConstants.CENTER);
        iconLabel.setBounds(90, 110, 100, 100); // x, y, width, height

        //날씨 정보 라벨
        weatherInfoLabel = new JLabel("로딩중...", SwingConstants.CENTER);
        weatherInfoLabel.setFont(new Font("굴림", Font.PLAIN, 20));
        weatherInfoLabel.setBounds(40, 200, 300, 30); // x, y, width, height

        // 기온 표시 라벨
        temperatureLabel = new JLabel("°C", SwingConstants.CENTER);
        temperatureLabel.setFont(new Font("굴림", Font.PLAIN, 45));
        temperatureLabel.setBounds(90, 140, 300, 50);

        //풍속 표시 라벨
        windSpeedLabel = new JLabel("풍속: m/s", SwingConstants.CENTER);
        windSpeedLabel.setFont(new Font("굴림", Font.PLAIN, 18));
        windSpeedLabel.setBounds(-20, 240, 300, 30);

        //습도 표시 라벨
        humidityLabel = new JLabel("습도: %", SwingConstants.CENTER);
        humidityLabel.setFont(new Font("굴림", Font.PLAIN, 18));
        humidityLabel.setBounds(120, 240, 300, 30);

        // northPanel에 컴포넌트 추가
        northPanel.add(backButton);
        northPanel.add(menuButton);
        northPanel.add(locationLabel);
        northPanel.add(iconLabel);
        northPanel.add(weatherInfoLabel);
        northPanel.add(temperatureLabel);
        northPanel.add(windSpeedLabel);
        northPanel.add(humidityLabel);

        // menuButton을 맨 뒤로 이동시키기
        northPanel.setComponentZOrder(menuButton, northPanel.getComponentCount() - 1);

        // 메인 패널에 northPanel 추가
        add(northPanel, BorderLayout.NORTH);

        // 초단기예보 패널 설정
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setPreferredSize(new Dimension(400, 200)); // 크기 축소

        // 스크롤 패널 설정
        scrollPane = new JScrollPane(centerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(400, 220)); // 스크롤 영역 높이 조정

        // 스크롤 패널을 메인 패널에 추가
        add(scrollPane, BorderLayout.CENTER);

        southPanel = new JPanel(new BorderLayout()); // LayoutManager를 BorderLayout으로 설정
        southPanel.setPreferredSize(new Dimension(400, 100)); // 적절한 크기 설정
        add(southPanel, BorderLayout.SOUTH);
    }

    // 선택된 지역 이름을 업데이트하는 메서드
    public void setLocationName(String locationName) {
        locationLabel.setText(locationName);
    }

    // 날씨 데이터를 기반으로 아이콘, 날씨 정보, 기온, 풍속, 습도 설정 (초단기실황)
    public void updateWeatherData(List<WeatherResponse.Item> weatherData) {
        boolean tempSet = false;
        boolean windSpeedSet = false;
        boolean humiditySet = false;

        for (WeatherResponse.Item item : weatherData) {
            // 강수 형태
            if ("PTY".equals(item.getCategory())) {
                if (item.getObsrValue() == 0) { // 맑음
                    // baseTime 대신 현재 시간을 사용
                    LocalTime currentTime = LocalTime.now();
                    int baseHour = currentTime.getHour(); // 현재 시스템 시간의 시간만 가져오기

                    if (baseHour >= 18 || baseHour < 6) { // 00:00 ~ 05:59 사이
                        iconLabel.setIcon(cloudMoonIcon); // 밤에 맑음
                        weatherInfoLabel.setText("맑음");
                    } else { // 그 외 시간대
                        iconLabel.setIcon(sunnyIcon);
                        weatherInfoLabel.setText("맑음");
                    }
                } else if (item.getObsrValue() == 1 || item.getObsrValue() == 2 || item.getObsrValue() == 5) {
                    iconLabel.setIcon(rainIcon);
                    weatherInfoLabel.setText("비");
                } else if (item.getObsrValue() == 3 || item.getObsrValue() == 6 || item.getObsrValue() == 7) {
                    iconLabel.setIcon(snowIcon);
                    weatherInfoLabel.setText("눈");
                }
            }

            // 기온 설정
            if ("T1H".equals(item.getCategory()) && !tempSet) {
                temperatureLabel.setText(String.format("%.1f°C", item.getObsrValue()));
                tempSet = true;
            }

            // 풍속 설정
            if ("WSD".equals(item.getCategory()) && !windSpeedSet) {
                windSpeedLabel.setText(String.format("풍속: %.1f m/s", item.getObsrValue()));
                windSpeedSet = true;
            }

            // 습도 설정
            if ("REH".equals(item.getCategory()) && !humiditySet) {
                humidityLabel.setText(String.format("습도: %.0f%%", item.getObsrValue()));
                humiditySet = true;
            }
        }
        repaint();
    }

    //단기예보 설정
    public void updateShortForecastData(List<WeatherResponse.Item> forecastData) {
        centerPanel.removeAll(); // 기존 예보 데이터 제거

        // 시간대별 날씨 데이터를 저장할 맵 초기화
        Map<String, WeatherForecastData> timeDataMap = new LinkedHashMap<>();

        // 전달받은 예보 데이터를 시간대별로 정리
        for (WeatherResponse.Item item : forecastData) {
            // 시간대 키 생성 ("12시", "15시" 등)
            String timeKey = item.getFcstTime().substring(0, 2) + "시";
            timeDataMap.putIfAbsent(timeKey, new WeatherForecastData()); // 시간대별 데이터 객체 생성

            // 강수 형태(PTY) 데이터 저장
            if ("PTY".equals(item.getCategory())) {
                timeDataMap.get(timeKey).setPtyValue((int) item.getFcstValue());
            }

            // 기온(T1H) 데이터 저장
            if ("T1H".equals(item.getCategory())) {
                timeDataMap.get(timeKey).setTemperature(item.getFcstValue());
            }
        }

        // 시간대별로 예보 패널 생성 및 추가
        for (Map.Entry<String, WeatherForecastData> entry : timeDataMap.entrySet()) {
            centerPanel.add(createForecastPanel(entry.getKey(), entry.getValue())); // 시간대별 패널 추가
            centerPanel.add(Box.createHorizontalStrut(10)); // 패널 간격 추가
        }

        // 패널 갱신
        centerPanel.revalidate(); // 레이아웃 업데이트
        centerPanel.repaint();    // 화면 다시 그리기

        // 단기예보 그래프 업데이트 호출
        updateShortForecastChart(forecastData);
    }


    //초단기예보 설정
    private JPanel createForecastPanel(String timeKey, WeatherForecastData data) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout()); // GridBagLayout을 사용해 중앙 정렬
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // 열 인덱스
        gbc.gridy = GridBagConstraints.RELATIVE; // 순차적으로 배치
        gbc.insets = new Insets(5, 5, 5, 5); // 패딩
        gbc.anchor = GridBagConstraints.CENTER; // 중앙 정렬

        JLabel timeLabel = new JLabel(timeKey, SwingConstants.CENTER);
        timeLabel.setFont(new Font("굴림", Font.BOLD, 14));

        JLabel icon = new JLabel();
        int hour = Integer.parseInt(timeKey.replace("시", "")); // 시간 추출
        if (hour >= 18 || hour < 6) {
            icon.setIcon(cloudMoonIcon); // 밤에 맑음
        } else {
            switch (data.getPtyValue()) {
                case 0: icon.setIcon(sunnyIcon); break; // 맑음
                case 1: case 2: case 5: icon.setIcon(rainIcon); break; // 비
                case 3: case 6: case 7: icon.setIcon(snowIcon); break; // 눈
                default: icon.setIcon(loadingIcon); break; // 기본 아이콘
            }
        }

        JLabel tempLabel = new JLabel(
                data.getTemperature() != Float.MIN_VALUE
                        ? String.format("%.1f°C", data.getTemperature())
                        : "N/A",
                SwingConstants.CENTER
        );
        tempLabel.setFont(new Font("굴림", Font.PLAIN, 14));

        panel.add(timeLabel, gbc);
        panel.add(icon, gbc);
        panel.add(tempLabel, gbc);

        return panel;
    }

    // SelectLocalPanel로 돌아갔을 때 초기화 진행을 위한 메서드
    public void resetWeatherData() {
        // 라벨 초기화
        locationLabel.setText("지역 이름");
        weatherInfoLabel.setText("로딩중...");
        iconLabel.setIcon(loadingIcon);
        temperatureLabel.setText("°C");
        windSpeedLabel.setText("풍속: m/s");
        humidityLabel.setText("습도: %");

        // 예보 데이터 초기화
        centerPanel.removeAll();
        centerPanel.revalidate();
        centerPanel.repaint();

        // 메뉴 초기화
        if (slidingMenuPanel != null) {
            slidingMenuPanel.resetMenu();
        }
    }


    //단기예보
    public void updateShortForecastChart(List<WeatherResponse.Item> forecastData) {
        // 데이터셋 초기화
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // 날짜별 최저/최고 기온 데이터를 저장할 Map
        Map<String, Double> minTemps = new LinkedHashMap<>(); // 날짜별 최저기온
        Map<String, Double> maxTemps = new LinkedHashMap<>(); // 날짜별 최고기온

        // 단기예보 데이터를 정리
        for (WeatherResponse.Item item : forecastData) {
            if ("TMN".equals(item.getCategory())) { // 최저기온
                minTemps.put(item.getFcstDate(), (double) item.getFcstValue());
            } else if ("TMX".equals(item.getCategory())) { // 최고기온
                maxTemps.put(item.getFcstDate(), (double) item.getFcstValue());
            }
        }

        // 데이터셋에 최저/최고기온 추가
        for (String date : minTemps.keySet()) {
            dataset.addValue(minTemps.get(date), "min", date);
            if (maxTemps.containsKey(date)) {
                dataset.addValue(maxTemps.get(date), "max", date);
            }
        }

        // JFreeChart를 이용해 그래프 생성
        JFreeChart chart = ChartFactory.createLineChart(
                "Temperature by date",   // 그래프 제목
                "date",           // X축 레이블
                "temperature (°C)",      // Y축 레이블
                dataset           // 데이터셋
        );

        // 렌더러 설정 (최소/최대 라인 표시)
        CategoryPlot plot = chart.getCategoryPlot();
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setDrawLines(true);
        plot.setRenderer(renderer);

        // 그래프를 ChartPanel에 추가
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(400, 200)); // 그래프 패널 크기 설정

        // 기존 그래프를 제거하고 새 그래프 추가
        southPanel.removeAll();
        southPanel.add(chartPanel, BorderLayout.CENTER); // BorderLayout.CENTER에 추가
        southPanel.revalidate();
        southPanel.repaint();
    }

}

class WeatherForecastData {
    private int ptyValue = -1; // 강수 형태 (기본값 -1: 유효하지 않음)
    private float temperature = Float.MIN_VALUE; // 기온 (기본값: 유효하지 않음)

    public void setPtyValue(int ptyValue) {
        this.ptyValue = ptyValue;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    // PTY와 T1H 값 모두 유효한지 확인
    public boolean hasAllValidData() {
        return ptyValue != -1 && temperature != Float.MIN_VALUE;
    }

    public int getPtyValue() {
        return ptyValue;
    }

    public float getTemperature() {
        return temperature;
    }
}

class SlidingMenuPanel extends JPanel {
    private int menuWidth = 150; // 메뉴 패널 너비
    private boolean isVisible = false; // 메뉴 보이기 상태
    private Timer timer;
    private int currentX = 400; // 초기 메뉴 위치 (프레임 너비)
    private ClientGui clientGui; // ClientGui 객체 참조
    private Client client;

    public SlidingMenuPanel(ClientGui clientGui, Client client) {
        this.clientGui = clientGui;
        this.client = client;

        setBackground(new Color(173, 216, 230)); // 하늘색 배경
        setLayout(null);
        setBounds(currentX, 0, menuWidth, 600); // 초기 위치

        JLabel menuTitle = new JLabel("메뉴", SwingConstants.CENTER);
        menuTitle.setFont(new Font("굴림", Font.BOLD, 20));
        menuTitle.setBounds(0, 10, menuWidth, 30);

        JButton searchHistoryButton = new JButton("검색기록");
        searchHistoryButton.setBounds(10, 60, 130, 40);
        searchHistoryButton.addActionListener(e -> {
            client.sendMessage(searchHistoryButton.getText());
            clientGui.showSearchHistoryPanel(); // 검색 기록 패널 표시
        });

        JButton exitButton = new JButton("프로그램 종료");
        exitButton.setBounds(10, 120, 130, 40);
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    SwingUtilities.getWindowAncestor(this), // 부모 프레임을 가져옴
                    "프로그램을 종료하시겠습니까?", // 메시지
                    "종료 확인", // 제목
                    JOptionPane.YES_NO_OPTION // 버튼 옵션
            );

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // 프로그램 종료
            }
        });


        add(menuTitle);
        add(searchHistoryButton);
        add(exitButton);

        // 타이머 설정
        timer = new Timer(10, e -> slideMenu());
    }

    public void toggleMenu() {
        isVisible = !isVisible; // 상태 반전
        timer.start(); // 타이머 시작
    }

    private void slideMenu() {
        if (isVisible) {
            if (currentX > 250) { // 목표 위치: 화면 오른쪽 끝 250px
                currentX -= 10; // 왼쪽으로 이동
                setBounds(currentX, 0, menuWidth, 600);
            } else {
                timer.stop(); // 목표 위치에 도달하면 타이머 중지
            }
        } else {
            if (currentX < 400) { // 메뉴 숨기기
                currentX += 10; // 오른쪽으로 이동
                setBounds(currentX, 0, menuWidth, 600);
            } else {
                timer.stop();
            }
        }
        repaint();
    }

    public void resetMenu() {
        isVisible = false; // 메뉴 상태를 닫힌 상태로 설정
        currentX = 400; // 메뉴를 화면 밖으로 이동
        setBounds(currentX, 0, menuWidth, 600); // 위치 업데이트
        repaint();
    }
}

class SearchHistoryPanel extends JPanel {

    private ClientGui clientGui; // ClientGui 객체 참조
    private JPanel recordPanel; // 검색 기록을 표시할 패널
    private JButton backButton;

    private ImageIcon backIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/backIcon.png");
    private ImageIcon backRolloverIcon = new ImageIcon("C:/Users/super/OneDrive/바탕 화면/대학/2-2/기초프로젝트II/Project2/backRolloverIcon.png");


    public SearchHistoryPanel(ClientGui clientGui) {
        this.clientGui = clientGui;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 상단 "검색기록" 및 뒤로가기 버튼 패널
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(400, 50));
        topPanel.setBackground(Color.WHITE);

        //backButton 구현
        backButton = new JButton("", backIcon);
        backButton.setBounds(20, 10, 48, 48);
        backButton.setBorderPainted(false); // 버튼 외곽선 제거
        backButton.setContentAreaFilled(false); // 내용영역 채우기 x
        backButton.setFocusPainted(false); // 버튼 선택 시 생기는 테두리 제거
        backButton.setRolloverIcon(backRolloverIcon);

        // 뒤로 가기 버튼 액션 리스너
        backButton.addActionListener(e -> {
            clientGui.cardLayout.show(clientGui.cardPanel, "SelectLocalPanel"); // 패널 전환
        });

        JLabel titleLabel = new JLabel("검색기록", SwingConstants.CENTER);
        titleLabel.setFont(new Font("굴림", Font.BOLD, 20));

        topPanel.add(backButton, BorderLayout.WEST);
        topPanel.add(titleLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // 검색 기록을 표시할 패널
        recordPanel = new JPanel();
        recordPanel.setLayout(new BoxLayout(recordPanel, BoxLayout.Y_AXIS));
        recordPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(recordPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    // 검색 기록 데이터를 업데이트하는 메서드
    public void updateSearchHistory(List<List<WeatherResponse.Item>> searchHistory) {
        recordPanel.removeAll(); // 기존 기록 제거

        for (List<WeatherResponse.Item> recordGroup : searchHistory) {
            recordPanel.add(createRecordPanel(recordGroup)); // 검색 기록 패널 추가
            recordPanel.add(Box.createVerticalStrut(10)); // 각 패널 간격 추가
        }

        recordPanel.revalidate();
        recordPanel.repaint();
    }

    // 개별 검색 기록 패널 생성
    private JPanel createRecordPanel(List<WeatherResponse.Item> recordGroup) {
        JPanel recordPanel = new JPanel();
        recordPanel.setLayout(null);
        recordPanel.setPreferredSize(new Dimension(350, 100));
        recordPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        recordPanel.setBackground(Color.WHITE);

        // 지역 이름 및 검색 일자 표시
        String level1 = recordGroup.get(0).getLevel1();
        String level2 = recordGroup.get(0).getLevel2();
        String createdAt = recordGroup.get(0).getCreatedAt();
        JLabel locationLabel = new JLabel(level1 + " " + level2 + " (" + createdAt + ")", SwingConstants.LEFT);
        locationLabel.setFont(new Font("굴림", Font.BOLD, 14));
        locationLabel.setBounds(10, 10, 300, 20);
        recordPanel.add(locationLabel);

        JLabel weatherInfo = new JLabel(formatWeatherInfo(recordGroup), SwingConstants.LEFT);
        weatherInfo.setFont(new Font("굴림", Font.PLAIN, 12));
        weatherInfo.setBounds(70, 40, 250, 50);
        recordPanel.add(weatherInfo);

        return recordPanel;
    }


    // 날씨 정보를 텍스트로 포맷팅
    private String formatWeatherInfo(List<WeatherResponse.Item> recordGroup) {
        StringBuilder info = new StringBuilder();
        for (WeatherResponse.Item item : recordGroup) {
            switch (item.getCategory()) {
                case "T1H": // 기온
                    info.append("기온: ").append(item.getObsrValue()).append("°C\n");
                    break;
                case "WSD": // 풍속
                    info.append("풍속: ").append(item.getObsrValue()).append("m/s\n");
                    break;
                case "REH": // 습도
                    info.append("습도: ").append(item.getObsrValue()).append("%\n");
                    break;
            }
        }
        return info.toString();
    }
}

