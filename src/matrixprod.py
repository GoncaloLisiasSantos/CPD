import time
import numpy as np

## MULTIPLICATION

def on_mult_line(m_ar, m_br):
    matrix_a = np.ones((m_ar, m_ar))
    matrix_b = np.array([[(i+1) for _ in range(m_br)] for i in range(m_br)])

    start_time = time.time()

    matrix_c = np.dot(matrix_a, matrix_b)

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Time: {elapsed_time} seconds")

    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    for i in range(min(1, m_ar)):
        for j in range(min(10, m_br)):
            print(matrix_c[i][j], end=" ")
        print()


## LINE MULTIPLICATION
def OnMultLine(m_ar, m_br):
    matrix_a = np.ones((m_ar, m_ar))
    matrix_b = np.array([[(i+1) for _ in range(m_br)] for i in range(m_br)])
    matrix_c = np.zeros((m_ar, m_ar))

    start_time = time.time()
    
    for i in range(m_ar):
        for k in range(m_br):
            for j in range(m_ar):
                matrix_c[i][j] += matrix_a[i][k]*matrix_b[k][j]

    end = time.time()

    return "{:.5f}".format(end - start_time)
        
        

def main():
    op = 1
    while op != 0:
        print("\nMenu:")
        print("________________________\n")
        print("1| Multiplication      ")
        print("2| Line Multiplication ")
        print("3| Block Multiplication")
        print("4| Exit                ")
        print("________________________\n")
        op = int(input("Selection?: "))
        if op == 0:
            break
        if op == 1:
            m_ar = int(input("Enter the number of rows: "))
            m_arr = int(input("Enter the number of columns: "))
            print(f"\nMatrix size: {m_ar}x{m_arr}")
            on_mult_line(m_ar, m_ar)
            break
        if op == 2:
            m_r = int(input("Enter the number of rows: "))
            print(f"\nMatrix size: {m_r}x{m_r}")
            print("Time:", OnMultLine(m_r,m_r), "seconds")
            break
        if op ==4:
            print("Exiting the program. Goodbye!")
            exit()
            

   

if __name__ == "__main__":
    main()
