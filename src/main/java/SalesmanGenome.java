import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SalesmanGenome implements Comparable {
    // Список городов в порядке их посещения
    // Эта последовательность представляет решение проблемы
    List<Integer> genome;

    // Цены на поездки удобны тем, что можно вычислить фитнесс-функцию
    int[][] travelPrices;

    // Хотя начальный город не меняет решения задачи, удобно просто выбрать один,
    // чтобы можно было быть уверенным, что он одинаков во всех геномах
    int startingCity;
    int numberOfCities = 0;
    int fitness;

    // Генерирует случайный геном
    public SalesmanGenome(int numberOfCities, int[][] travelPrices, int startingCity) {
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.numberOfCities = numberOfCities;
        genome = randomSalesman();
        fitness = this.calculateFitness();
    }

    // Генерирует заданный пользователем геном
    public SalesmanGenome(List<Integer> permutationOfCities, int numberOfCities, int[][] travelPrices, int startingCity) {
        genome = permutationOfCities;
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.numberOfCities = numberOfCities;
        fitness = this.calculateFitness();
    }

    public int calculateFitness() {
        int fitness = 0;
        int currentCity = startingCity;

        // Расчет стоимости пути
        for (int gene : genome) {
            fitness += travelPrices[currentCity][gene];
            currentCity = gene;
        }

        // Мы должны добавить возвращение в начальный город, чтобы завершить круг
        // в геноме отсутствует начальный город, и индексация начинается с 0, поэтому мы вычитаем 2
        fitness += travelPrices[genome.get(numberOfCities - 2)][startingCity];
        return fitness;
    }

    // Генерирует случайный геном
    // Геномы — это перестановки списка городов, за исключением начального города,
    // поэтому мы добавляем их все в список и перемешиваем
    private List<Integer> randomSalesman() {
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < numberOfCities; i++) {
            if(i != startingCity) {
                result.add(i);
            }
        }
        Collections.shuffle(result);
        return result;
    }

    public List<Integer> getGenome() {
        return genome;
    }

    public int getStartingCity() {
        return startingCity;
    }

    public int getFitness() {
        return fitness;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Path: ");
        sb.append(startingCity);
        for ( int gene: genome ) {
            sb.append(" ");
            sb.append(gene);
        }
        sb.append(" ");
        sb.append(startingCity);
        sb.append("\nLength: ");
        sb.append(this.fitness);
        return sb.toString();
    }


    @Override
    public int compareTo(Object o) {
        SalesmanGenome genome = (SalesmanGenome) o;
        if(this.fitness > genome.getFitness()) {
            return 1;
        } else if(this.fitness < genome.getFitness()) {
            return -1;
        } else {
            return 0;
        }
    }
}
