import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SalesmanGenome implements Comparable {
    // Список міст у порядку їх відвідування
    // Ця послідовність є вирішенням проблеми
    List<Integer> genome;

    // Ціни на поїздки зручні тим, що можна обчислити фітнес-функцію
    int[][] travelPrices;

    // Хоча початкове місто не змінює рішення задачі, зручно просто вибрати одне,
    // щоб можна було бути впевненим, що він однаковий у всіх геномах
    int startingCity;
    int numberOfCities;
    int fitness;

    // Генерує випадковий геном
    public SalesmanGenome(int numberOfCities, int[][] travelPrices, int startingCity) {
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.numberOfCities = numberOfCities;
        genome = randomSalesman();
        fitness = this.calculateFitness();
    }

    // Генерує заданий користувачем геном
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

        // Розрахунок вартості шляху
        for (int gene : genome) {
            fitness += travelPrices[currentCity][gene];
            currentCity = gene;
        }

        // Ми повинні додати повернення до початкового міста, щоб завершити коло
        // В геномі відсутнє початкове місто, і індексація починається з 0, тому ми віднімаємо 2
        fitness += travelPrices[genome.get(numberOfCities - 2)][startingCity];
        return fitness;
    }

    // Генерує випадковий геном
    // Геноми - це перестановки списку міст, за винятком початкового міста,
    // тому ми додаємо їх у список і перемішуємо
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
        sb.append("Шлях: ");
        sb.append(startingCity + 1);
        for ( int gene: genome ) {
            sb.append(" -> ");
            sb.append(gene + 1);
        }
        sb.append(" -> ");
        sb.append(startingCity + 1);
        sb.append("\nДлина: ");
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
