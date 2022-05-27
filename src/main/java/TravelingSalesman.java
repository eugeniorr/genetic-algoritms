import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TravelingSalesman {

    // Размер поколения - это количество геномов / особей в каждом поколении / популяции.
    // Этот параметр также часто называют численностью популяции.
    private int generationSize;

    // Размер генома - это длина ArrayList генома, которая будет равна numberOfCities - 1.
    // Две переменные разделены для ясности в остальной части кода.
    // Этот параметр также часто называют длиной хромосомы.
    private int genomeSize;
    private int numberOfCities;

    // Размер репродукции — это количество геномов, которые будут выбраны для воспроизведения,
    // чтобы создать следующее поколение. Этот параметр также часто называют скоростью кроссинговера.
    private int reproductionSize;

    // Максимальное количество итераций - это максимальное количество поколений,
    // в которых программа будет развиваться до завершения, в случае, если до этого не будет сходимости.
    private int maxIterations;

    //Скорость мутации относится к частоте мутаций при создании нового поколения.
    private float mutationRate;

    // Размер турнира для турнирного варианта селекции.
    private int tournamentSize;

    // Тип селекции (турнир или рулетка)
    private SelectionType selectionType;

    // Цены на проезд - это матрица цен на проезд между каждыми двумя городами - эта матрица
    // будет иметь нули по диагонали и симметричные значения в нижнем и верхнем треугольнике.
    private int[][] travelPrices;

    // Индекс начального города.
    private int startingCity;

    // Целевая пригодность — это приспособленность, которой должен достичь лучший геном в соответствии с
    // целевой функцией (которая в нашей реализации будет такой же, как и функция приспособленности),
    // чтобы программа завершилась досрочно. Иногда установка целевого фитнеса может сократить программу,
    // если нам нужно только определенное значение или лучше. Здесь, если мы хотим, чтобы наши затраты были ниже
    // определенного числа, но не важно, насколько низко, мы можем использовать его для установки этого порога.
    private int targetFitness;

    public TravelingSalesman(
            int numberOfCities, SelectionType selectionType, int[][] travelPrices, int startingCity, int targetFitness
    ) {
        this.numberOfCities = numberOfCities;
        this.genomeSize = numberOfCities - 1;
        this.selectionType = selectionType;
        this.travelPrices = travelPrices;
        this.startingCity = startingCity;
        this.targetFitness = targetFitness;

        generationSize = 5000;
        reproductionSize = 200;
        maxIterations = 1000;
        mutationRate = 0.1f;
        tournamentSize = 40;
    }

    public List<SalesmanGenome> initialPopulation() {
        List<SalesmanGenome> population = new ArrayList<>();
        for(int i = 0; i < generationSize; i++) {
            population.add(new SalesmanGenome(numberOfCities, travelPrices, startingCity));
        }
        return population;
    }

    // Мы выбираем геномы reproductionSize на основе метода, предопределенного в атрибуте selectionType
    public List<SalesmanGenome> selection(List<SalesmanGenome> population) {
        List<SalesmanGenome> selected = new ArrayList<>();

        for(int i = 0; i < reproductionSize; i++) {
            if(selectionType == SelectionType.ROULETTE) {
                selected.add(rouletteSelection(population));
            } else if(selectionType == SelectionType.TOURNAMENT) {
                selected.add(tournamentSelection(population));
            }
        }

        return selected;
    }

    public SalesmanGenome rouletteSelection(List<SalesmanGenome> population) {
        int totalFitness = population.stream().map(SalesmanGenome::getFitness).mapToInt(Integer::intValue).sum();

        // Мы выбираем случайное значение - точку на нашем колесе рулетки
        Random random = new Random();
        int selectedValue = random.nextInt(totalFitness);

        // Так как мы делаем минимизацию, нам нужно использовать взаимное значение, чтобы вероятность
        // выбора генома была обратно пропорционально его пригодности:
        // чем меньше пригодность - тем выше вероятность
        float recValue = (float) 1 / selectedValue;

        // Мы суммируем значения, пока не достигнем recValue, и выбираем геном, перешагнувший порог
        float currentSum = 0;
        for(SalesmanGenome genome : population) {
            currentSum +=  1. / genome.getFitness();
            if(currentSum >= recValue) {
                return genome;
            }
        }

        // Если возврат не произошел в цикле выше, мы просто выбираем наугад
        int selectRandom = random.nextInt(generationSize);
        return population.get(selectRandom);
    }

    // Вспомогательная функция для выбора n случайных элементов из совокупности чтобы мы могли ввести их в турнир
    public static <E> List<E> pickNRandomElements(List<E> list, int n) {
        Random r = new Random();
        int length = list.size();

        if (length < n) {
            return null;
        }

        for (int i = length - 1; i >= length - n; --i) {
            Collections.swap(list, i , r.nextInt(i + 1));
        }
        return list.subList(length - n, length);
    }

    // Простая реализация детерминированного турнира - лучший геном всегда выигрывает
    public SalesmanGenome tournamentSelection(List<SalesmanGenome> population) {
        List<SalesmanGenome> selected = pickNRandomElements(population,tournamentSize);
        return Collections.min(selected);
    }

    public List<SalesmanGenome> crossover(List<SalesmanGenome> parents) {
        // Подготовка
        Random random = new Random();
        int breakpoint = random.nextInt(genomeSize);
        List<SalesmanGenome> children = new ArrayList<>();

        // Копируем родительские геномы чтобы не модифицировать, если бы они были
        // выбраны для участия в кроссовере несколько раз
        List<Integer> parent1Genome = new ArrayList<>(parents.get(0).getGenome());
        List<Integer> parent2Genome = new ArrayList<>(parents.get(1).getGenome());

        // Создание первого потомка
        for(int i = 0; i < breakpoint; i++) {
            int newVal;
            newVal = parent2Genome.get(i);
            Collections.swap(parent1Genome, parent1Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent1Genome, numberOfCities, travelPrices, startingCity));
        parent1Genome = parents.get(0).getGenome(); // Сброс отредактированного родителя

        // Создание второго потомка
        for(int i = breakpoint; i < genomeSize; i++) {
            int newVal = parent1Genome.get(i);
            Collections.swap(parent2Genome,parent2Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent2Genome, numberOfCities, travelPrices, startingCity));

        return children;
    }

    // Мутация довольно проста - если мы пройдем проверку вероятности, мы мутируем,
    // поменяв местами два города в геноме
    // В противном случае мы просто возвращаем исходный геном
    public SalesmanGenome mutate(SalesmanGenome salesman) {
        Random random = new Random();
        float mutate = random.nextFloat();
        if(mutate < mutationRate) {
            List<Integer> genome = salesman.getGenome();
            Collections.swap(genome, random.nextInt(genomeSize), random.nextInt(genomeSize));
            return new SalesmanGenome(genome, numberOfCities, travelPrices, startingCity);
        }
        return salesman;
    }

    // Мы используем алгоритм поколений, поэтому мы создаем совершенно новую популяцию детей
    public List<SalesmanGenome> createGeneration(List<SalesmanGenome> population) {
        List<SalesmanGenome> generation = new ArrayList<>();
        int currentGenerationSize = 0;
        while(currentGenerationSize < generationSize) {
            List<SalesmanGenome> parents = pickNRandomElements(population,2);
            List<SalesmanGenome> children = crossover(parents);
            children.set(0, mutate(children.get(0)));
            children.set(1, mutate(children.get(1)));
            generation.addAll(children);
            currentGenerationSize += 2;
        }
        return generation;
    }

    // Мы прекращаем действие при следующих условиях:
    //    1. Количество поколений достигло maxIterations
    //    2. Лучшая длина пути генома меньше, чем длина целевого пути
    public SalesmanGenome optimize() {
        List<SalesmanGenome> population = initialPopulation();
        SalesmanGenome globalBestGenome = population.get(0);
        for(int i = 0; i < maxIterations; i++) {
            List<SalesmanGenome> selected = selection(population);
            population = createGeneration(selected);
            globalBestGenome = Collections.min(population);
            if(globalBestGenome.getFitness() < targetFitness)
                break;
        }
        return globalBestGenome;
    }
}
