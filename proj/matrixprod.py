import time
import numpy as np

## MULTIPLICATION

def on_mult_line(m_ar, m_br):
    pha = np.ones((m_ar, m_ar))
    phb = np.array([[(i+1) for _ in range(m_br)] for i in range(m_br)])

    start_time = time.time()

    phc = np.dot(pha, phb)

    end_time = time.time()
    elapsed_time = end_time - start_time
    print(f"Time: {elapsed_time} seconds")

    # Display 10 elements of the result matrix to verify correctness
    print("Result matrix:")
    for i in range(min(1, m_ar)):
        for j in range(min(10, m_br)):
            print(phc[i][j], end=" ")
        print()


## LINE MULTIPLICATION
        

def main():

    m_ar = int(input("Enter the number of rows: "))
    m_arr = int(input("Enter the number of columns: "))
    print(f"\nMatrix size: {m_ar}x{m_arr}")
    on_mult_line(m_ar, m_ar)

if __name__ == "__main__":
    main()
