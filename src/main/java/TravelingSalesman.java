import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TravelingSalesman {

    // Розмір покоління – це кількість геномів / особин у кожному поколінні / популяції
    // Цей параметр також часто називають чисельністю популяції
    private int generationSize;

    // Розмір геному - це довжина генома ArrayList, яка дорівнює numberOfCities - 1
    // Дві змінні розділені для ясності у решті коду.
    // Цей параметр також часто називають довжиною хромосоми
    private int genomeSize;
    private int numberOfCities;

    // Розмір репродукції — кількість геномів, які будуть обрані для відтворення,
    // щоб створити наступне покоління. Цей параметр також часто називають швидкістю кросинговеру
    private int reproductionSize;

    // Максимальна кількість ітерацій – це максимальна кількість поколінь,
    // у яких програма буде розвиватися до завершення, якщо до цього не буде збіжності
    private int maxIterations;

    // Швидкість мутації відноситься до частоти мутацій при створенні нового покоління
    private float mutationRate;

    // Розмір турніру для турнірного варіанта селекції
    private int tournamentSize;

    // Тип селекції (турнір або рулетка)
    private SelectionType selectionType;

    // Ціни на проїзд – це матриця цін на проїзд між кожними двома містами – ця матриця
    // матиме нулі по діагоналі та симетричні значення у нижньому та верхньому трикутнику
    private int[][] travelPrices;

    // Індекс початкового міста
    private int startingCity;

    // Цільова придатність - це пристосованість, якої повинен досягти найкращий геном відповідно до
    // цільової функції (яка в нашій реалізації буде такою ж, як і функція пристосованості),
    // щоб програма завершилася достроково. Іноді встановлення цільового фітнесу може скоротити програму,
    // якщо нам потрібне лише певне значення або краще. Тут, якщо ми хочемо, щоб наші витрати були нижчими
    // певного числа, але не важливо, наскільки низько ми можемо використовувати його для встановлення цього порога
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

    // Ми вибираємо геноми reproductionSize на основі методу, визначеного в атрибуті selectionType
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

        // Ми вибираємо випадкове значення – точку на нашому колесі рулетки
        Random random = new Random();
        int selectedValue = random.nextInt(totalFitness);

        // Так як ми робимо мінімізацію, нам потрібно використовувати взаємне значення, щоб ймовірність
        // вибору геному була обернено пропорційно його придатності:
        // чим менша придатність - тим вище ймовірність
        float recValue = (float) 1 / selectedValue;

        // Ми підсумовуємо значення, поки не досягнемо recValue, і вибираємо геном, що переступив поріг
        float currentSum = 0;
        for(SalesmanGenome genome : population) {
            currentSum +=  1. / genome.getFitness();
            if(currentSum >= recValue) {
                return genome;
            }
        }

        // Якщо повернення не відбулося в циклі вище, ми просто вибираємо навмання
        int selectRandom = random.nextInt(generationSize);
        return population.get(selectRandom);
    }

    // Допоміжна функція для вибору n випадкових елементів із сукупності, щоб ми могли ввести їх у турнір
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

    // Проста реалізація детермінованого турніру – найкращий геном завжди виграє
    public SalesmanGenome tournamentSelection(List<SalesmanGenome> population) {
        List<SalesmanGenome> selected = pickNRandomElements(population,tournamentSize);
        return Collections.min(selected);
    }

    public List<SalesmanGenome> crossover(List<SalesmanGenome> parents) {
        // Підготовка
        Random random = new Random();
        int breakpoint = random.nextInt(genomeSize);
        List<SalesmanGenome> children = new ArrayList<>();

        // Копіюємо батьківські геноми, щоб не модифікувати, якби вони були
        // вибрані для участі в кросовері кілька разів
        List<Integer> parent1Genome = new ArrayList<>(parents.get(0).getGenome());
        List<Integer> parent2Genome = new ArrayList<>(parents.get(1).getGenome());

        // Створення першого нащадка
        for(int i = 0; i < breakpoint; i++) {
            int newVal = parent2Genome.get(i);
            Collections.swap(parent1Genome, parent1Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent1Genome, numberOfCities, travelPrices, startingCity));
        parent1Genome = parents.get(0).getGenome(); // Скидання відредагованого батька

        // Створення другого нащадка
        for(int i = breakpoint; i < genomeSize; i++) {
            int newVal = parent1Genome.get(i);
            Collections.swap(parent2Genome,parent2Genome.indexOf(newVal), i);
        }
        children.add(new SalesmanGenome(parent2Genome, numberOfCities, travelPrices, startingCity));

        return children;
    }

    // Мутація досить проста - якщо ми пройдемо перевірку ймовірності, ми мутуємо,
    // помінявши місцями два міста у геномі
    // Інакше ми просто повертаємо вихідний геном
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

    // Ми використовуємо алгоритм поколінь, тому ми створюємо нову популяцію дітей
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

    // Ми припиняємо дію за таких умов:
    //    1. Кількість поколінь досягла maxIterations
    //    2. Найкраща довжина шляху геному менша, ніж довжина цільового шляху
    public SalesmanGenome optimize() {
        List<SalesmanGenome> population = initialPopulation();
        SalesmanGenome globalBestGenome = population.get(0);
        for(int i = 0; i < maxIterations; i++) {
            List<SalesmanGenome> selected = selection(population);
            population = createGeneration(selected);
            globalBestGenome = Collections.min(population);
            if(globalBestGenome.getFitness() < targetFitness)
                return globalBestGenome;
        }
        return globalBestGenome;
    }
}
