import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void printTravelPrices(int[][] travelPrices, int numberOfCities) {
        for(int i = 0; i < numberOfCities; i++) {
            for(int j = 0; j < numberOfCities; j++) {
                System.out.print(travelPrices[i][j]);
                if(travelPrices[i][j] / 10 == 0) {
                    System.out.print("  ");
                } else {
                    System.out.print(' ');
                }
            }
            System.out.println("");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ви самі хочете ввести дані? (так/ні): ");
        String userAnswer = scanner.nextLine();
        if (userAnswer.equals("так")) {
            System.out.println("Введіть кількість міст: ");
            int numberOfCities = scanner.nextInt();
            int[][] travelPrices = new int[numberOfCities][numberOfCities];
            System.out.println("Введіть коштовність переїзду між містами (з 1-го у 2-ге, з 2-го у 3-тє і т.д.): ");
            for(int i = 0; i < numberOfCities; i++) {
                for(int j = 0; j <= i; j++) {
                    if(i == j) {
                        travelPrices[i][j] = 0;
                    } else {
                        travelPrices[i][j] = scanner.nextInt();
                        travelPrices[j][i] = travelPrices[i][j];
                    }
                }
            }
            scanner.close();
            printTravelPrices(travelPrices, numberOfCities);

            long time = System.currentTimeMillis();
            TravelingSalesman geneticAlgorithm = new TravelingSalesman(numberOfCities, SelectionType.ROULETTE, travelPrices, 0, 0);
            SalesmanGenome result = geneticAlgorithm.optimize();
            System.out.println(result);
            System.out.println("Час рахування: " + (System.currentTimeMillis() - time));
        } else if (userAnswer.equals("ні")) {
            int numberOfCities = 4;
            int[][] travelPrices = new int[numberOfCities][numberOfCities];
            for(int i = 0; i < numberOfCities; i++) {
                for(int j = 0; j <= i; j++) {
                    Random rand = new Random();
                    if(i == j) {
                        travelPrices[i][j] = 0;
                    } else {
                        travelPrices[i][j] = rand.nextInt(100);
                        travelPrices[j][i] = travelPrices[i][j];
                    }
                }
            }

            printTravelPrices(travelPrices, numberOfCities);

            long time = System.currentTimeMillis();
            TravelingSalesman geneticAlgorithm = new TravelingSalesman(numberOfCities, SelectionType.ROULETTE, travelPrices, 0, 0);
            SalesmanGenome result = geneticAlgorithm.optimize();
            System.out.println(result);
            System.out.println("Час рахування: " + (System.currentTimeMillis() - time));
        } else {
            System.out.println("Відповідь некоректна!");
        }
    }
}

