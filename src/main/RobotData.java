package main;

public class RobotData {
    private String fileName;
    private int totalFitness;
    private int robocodeScore;
    private int ramDamageScore;
    private int bulletDamageScore;
    private int timesFirst;
    private int timesSecond;
    private int timesThird;

    public RobotData(String fileName){
        this.fileName = fileName;
    }

    public void calculateTotalFitness(int ramDamageMultiplier, int bulletDamageMultiplier, int timesFirstMultiplier,
                                        int timesSecondMultiplier, int timesThirdMultiplier){
        setTotalFitness(robocodeScore +
                (ramDamageScore * ramDamageMultiplier) +
                (bulletDamageScore * bulletDamageMultiplier) +
                (timesFirst * timesFirstMultiplier) +
                (timesSecond * timesSecondMultiplier) +
                (timesThird * timesThirdMultiplier)
        );
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getTotalFitness() {
        return totalFitness;
    }

    public void setTotalFitness(int totalFitness) {
        this.totalFitness = totalFitness;
    }

    public int getRamDamageScore() {
        return ramDamageScore;
    }

    public void setRamDamageScore(int ramDamageScore) {
        this.ramDamageScore = ramDamageScore;
    }

    public int getBulletDamageScore() {
        return bulletDamageScore;
    }

    public void setBulletDamageScore(int bulletDamageScore) {
        this.bulletDamageScore = bulletDamageScore;
    }

    public int getTimesFirst() {
        return timesFirst;
    }

    public void setTimesFirst(int timesFirst) {
        this.timesFirst = timesFirst;
    }

    public int getTimesSecond() {
        return timesSecond;
    }

    public void setTimesSecond(int timesSecond) {
        this.timesSecond = timesSecond;
    }

    public int getTimesThird() {
        return timesThird;
    }

    public void setTimesThird(int timesThird) {
        this.timesThird = timesThird;
    }

    public int getRobocodeScore() {
        return robocodeScore;
    }

    public void setRobocodeScore(int robocodeScore) {
        this.robocodeScore = robocodeScore;
    }

}
