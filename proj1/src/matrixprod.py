import time
import numpy as np

## MULTIPLICATION

def mult(size):
    matrix_a = np.ones((size, size))
    matrix_b = np.array([[(i+1) for _ in range(size)] for i in range(size)])

    start_time = time.time()

    matrix_c = np.dot(matrix_a, matrix_b)

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Time: {elapsed_time} seconds")

    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    for i in range(min(1, size)):
        for j in range(min(10, size)):
            print(matrix_c[i][j], end=" ")
        print()


## LINE MULTIPLICATION
def line_mult(size):
    matrix_a = np.ones((size, size))
    matrix_b = np.array([[(i+1) for _ in range(size)] for i in range(size)])
    matrix_c = np.zeros((size, size))

    start_time = time.time()

    for i in range(size):
        for k in range(size):
            for j in range(size):
                matrix_c[i][j] += matrix_a[i][k] * matrix_b[k][j]

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Time: {elapsed_time} seconds")

    # Display 10 elements of the result matrix to verify correctness
    #print("Result matrix:")
    #for i in range(min(1, size)):
    #    for j in range(min(10, size)):
    #        print(matrix_c[i][j], end=" ")
    #    print()

## BLOCK MULTIPLICATION
def block_mult(size, block_size):
    matrix_a = np.ones((size, size))
    matrix_b = np.array([[(i+1) for _ in range(size)] for i in range(size)])
    matrix_c = np.zeros((size, size))

    start_time = time.time()

    for i0 in range(0, size, block_size):
        for j0 in range(0, size, block_size):
            for k0 in range(0, size, block_size):
                for i in range(i0, min(i0 + block_size, size)):
                    for j in range(j0, min(j0 + block_size, size)):
                        for k in range(k0, min(k0 + block_size, size)):
                            matrix_c[i, k] += matrix_a[i, j] * matrix_b[j, k]

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Time: {elapsed_time} seconds")

    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    for i in range(min(1, size)):
        for j in range(min(10, size)):
            print(matrix_c[i][j], end=" ")
        print()

def main():
    option = 1
    while option != 0:
        print("\nMenu:")
        print("________________________\n")
        print("1| Multiplication      ")
        print("2| Line Multiplication ")
        print("3| Block Multiplication")
        print("4| Exit                ")
        print("________________________\n")
        option = int(input("Selection?: "))

        if option < 1 or option > 4:
            print("Enter a valid input!")
            main()

        if option == 1:
            size = int(input("Enter the number of rows/cols: "))
            # m_arr = int(input("Enter the number of columns: "))
            print(f"\nMatrix size: {size}x{size}")
            mult(size)
            '''
            for size in range(600, 3400, 400):
                print(f"\nMatrix size: {size}x{size}")
                mult(size)
            '''
            break

        if option == 2:
            size = int(input("Enter the number of rows/cols: "))
            # m_arr = int(input("Enter the number of columns: "))
            print(f"\nMatrix size: {size}x{size}")
            line_mult(size)
            '''
            for size in range(600, 3400, 400):
                print(f"\nMatrix size: {size}x{size}")
                line_mult(size)
            '''
            break

        if option == 3:
            size = int(input("Enter the number of rows/cols: "))
            block_size = int(input("Enter the size of each block: "))
            # m_arr = int(input("Enter the number of columns: "))
            print(f"\nMatrix size: {size}x{size}")
            print(f"\nBlock size: {block_size}")
            block_mult(size, block_size)
            '''
            for size in range(600, 3400, 400):
                print(f"\nMatrix size: {size}x{size}")
                print(f"\nBlock size: {block_size}")
                block_mult(size, block_size)
            '''
            break
        if option == 4:
            print("Exiting the program. Goodbye!")
            exit()
            

   

if __name__ == "__main__":
    main()
