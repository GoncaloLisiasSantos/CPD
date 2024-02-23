import numpy as np
import time

def OnMult(m_r):
    pha = np.ones((m_r, m_r))
    phb = np.repeat(np.arange(1, m_r + 1, dtype=np.float64).reshape(-1, 1), m_r, axis=1)
    phc = np.zeros((m_r, m_r))

    time1 = time.time()

    for i in range(m_r):
        for j in range(m_r):
            phc[i,j] = np.dot(pha[i,:], phb[:,j])

    #phc = np.matmul(pha, phb)

    print("Time:", time.time() - time1, "seconds")

    #// display 10 elements of the result matrix to verify correctness
    print("Result matrix: ")
    print(phc[0,:10])
    print()

def OnMultLine(m_r):
    pha = np.ones((m_r, m_r))
    phb = np.repeat(np.arange(1, m_r + 1, dtype=np.float64).reshape(-1, 1), m_r, axis=1)
    phc = np.zeros((m_r, m_r))

    time1 = time.time()

    for j in range(m_r):
        for i in range(m_r):
            phc[i,:] += pha[i,j] * phb[j,:]

    print("Time:", time.time() - time1, "seconds")

    #// display 10 elements of the result matrix to verify correctness
    print("Result matrix: ")
    print(phc[0,:10])
    print()

def OnMultTranspose(m_r):
    pha = np.ones((m_r, m_r))
    phb = np.repeat(np.arange(1, m_r + 1, dtype=np.float64).reshape(-1, 1), m_r, axis=1)
    phb = np.transpose(phb)
    phc = np.zeros((m_r, m_r))

    time1 = time.time()

    for j in range(m_r):
        for i in range(m_r):
            phc[i,j] = np.dot(pha[i,:], phb[j,:])

    print("Time:", time.time() - time1, "seconds")

    #// display 10 elements of the result matrix to verify correctness
    print("Result matrix: ")
    print(phc[0,:10])
    print()



def OnMultInvalid(m_r):
    return None

if __name__ == "__main__":
    jumptable = {
        "1": OnMult,
        "2": OnMultLine,
        "3": OnMultTranspose,

    }
    op = ""
    while (op != "0"):
        op = input("\n1. Multiplication\n2. Line Multiplication\n3. Transpose Multiplication\n4. Numpy Multiplication\nSelection?: ");
        if (op == "0"):
            break

        m_r = int(input("Dimensions: lins=cols ? "))

        jumptable.get(op, OnMultInvalid)(m_r)