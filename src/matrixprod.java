import java.util.Scanner;

public class MatrixMultiplication {

    // MULTIPLICATION
    public static void mult(int size) {
        double[][] matrix_a = new double[size][size];
        double[][] matrix_b = new double[size][size];

        // Initializing matrix_a with ones
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_a[i][j] = 1;
            }
        }

        // Initializing matrix_b with increasing values
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_b[i][j] = i + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        double[][] matrix_c = new double[size][size];

        // Matrix multiplication
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                for (int k = 0; k < size; k++) {
                    matrix_c[i][j] += matrix_a[i][k] * matrix_b[k][j];
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        System.out.println("Time: " + elapsedTime + " seconds");

        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix:");
        for (int i = 0; i < Math.min(1, size); i++) {
            for (int j = 0; j < Math.min(10, size); j++) {
                System.out.print(matrix_c[i][j] + " ");
            }
            System.out.println();
        }
    }

    // LINE MULTIPLICATION
    public static void lineMult(int size) {
        double[][] matrix_a = new double[size][size];
        double[][] matrix_b = new double[size][size];

        // Initializing matrix_a with ones
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_a[i][j] = 1;
            }
        }

        // Initializing matrix_b with increasing values
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_b[i][j] = i + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        double[][] matrix_c = new double[size][size];

        // Matrix multiplication
        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                for (int j = 0; j < size; j++) {
                    matrix_c[i][j] += matrix_a[i][k] * matrix_b[k][j];
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        System.out.println("Time: " + elapsedTime + " seconds");

        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix:");
        for (int i = 0; i < Math.min(1, size); i++) {
            for (int j = 0; j < Math.min(10, size); j++) {
                System.out.print(matrix_c[i][j] + " ");
            }
            System.out.println();
        }
    }

    // BLOCK MULTIPLICATION
    public static void blockMult(int size, int blockSize) {
        double[][] matrix_a = new double[size][size];
        double[][] matrix_b = new double[size][size];

        // Initializing matrix_a with ones
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_a[i][j] = 1;
            }
        }

        // Initializing matrix_b with increasing values
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix_b[i][j] = i + 1;
            }
        }

        long startTime = System.currentTimeMillis();

        double[][] matrix_c = new double[size][size];

        // Matrix multiplication
        for (int i0 = 0; i0 < size; i0 += blockSize) {
            for (int j0 = 0; j0 < size; j0 += blockSize) {
                for (int k0 = 0; k0 < size; k0 += blockSize) {
                    for (int i = i0; i < Math.min(i0 + blockSize, size); i++) {
                        for (int j = j0; j < Math.min(j0 + blockSize, size); j++) {
                            for (int k = k0; k < Math.min(k0 + blockSize, size); k++) {
                                matrix_c[i][j] += matrix_a[i][k] * matrix_b[k][j];
                            }
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        double elapsedTime = (endTime - startTime) / 1000.0;
        System.out.println("Time: " + elapsedTime + " seconds");

        // Display 10 elements of the result matrix to verify correctness
        System.out.println("Result matrix:");
        for (int i = 0; i < Math.min(1, size); i++) {
            for (int j = 0; j < Math.min(10, size); j++) {
                System.out.print(matrix_c[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int option = 1;
        while (option != 0) {
            System.out.println("\nMenu:");
            System.out.println("________________________\n");
            System.out.println("1| Multiplication      ");
            System.out.println("2| Line Multiplication ");
            System.out.println("3| Block Multiplication");
            System.out.println("4| Exit                ");
            System.out.println("________________________\n");
            System.out.print("Selection?: ");
            option = scanner.nextInt();

            switch (option) {
                case 1:
                    System.out.print("Enter the number of rows/cols: ");
                    int size = scanner.nextInt();
                    System.out.println("\nMatrix size: " + size + "x" + size);
                    mult(size);
                    break;
                case 2:
                    System.out.print("Enter the number of rows/cols: ");
                    size = scanner.nextInt();
                    System.out.println("\nMatrix size: " + size + "x" + size);
                    lineMult(size);
                    break;
                case 3:
                    System.out.print("Enter the number of rows/cols: ");
                    size = scanner.nextInt();
                    System.out.print("Enter the size of each block: ");
                    int blockSize = scanner.nextInt();
                    System.out.println("\nMatrix size: " + size + "x" + size);
                    System.out.println("\nBlock size: " + blockSize);
                    blockMult(size, blockSize);
                    break;
                case 4:
                    System.out.println("Exiting the program. Goodbye!");
                    System.exit(0);
                default:
                    System.out.println("Enter a valid input!");
                    break;
            }
        }
    }
}
