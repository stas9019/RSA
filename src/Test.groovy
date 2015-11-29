import java.security.SecureRandom

/**
 * Created by stas on 23.11.15.
 */



class Test {

    static int CRT = 1


    void compareTraditionalAndCRT()
    {
        def l
        def date
        def rsa
        def ciphertext

        def L = [256, 512, 1024, 2048, 3072/*7680*/]

        def text1 = "The quick brown fox jumps over the lazy dog";
        BigInteger plaintext = new BigInteger(text1.getBytes());


        for(int k = 2; k<=8; k++)
            for(int i = 0; i < L.size(); i++)
            {
                l = L[i]

                date = new Date();
                println  "($k, $l) CRT started: $date \n"

                rsa = new RSA(k, l, CRT)

                ciphertext = rsa.encrypt(plaintext)
                plaintext = rsa.decryptCRT(ciphertext)

                date = new Date();
                println "($k, $l) CRT finishted: $date\n"



                println "==================================================\n"



                date = new Date();

                println  "($k, $l) Traditional started: $date\n"
                rsa = new RSA(k, l)

                ciphertext = rsa.encrypt(plaintext)
                plaintext = rsa.decrypt(ciphertext)

                date = new Date();
                println "($k, $l) Traditional finishted: $date\n"



                println "\n=================================================="
                println "==================================================\n"
            }




    }

    void generatePrimes()
    {
        def Bits = [/*256, 512, 1024,*/ 2048, 3072,7680]
        def r = new SecureRandom()

        for(int k = 1; k<=8; k++)
            for(int i = 0; i < Bits.size(); i++)
            {
                def bits = Bits[i]
                RSA.generatePrimesParallel(k, bits)


                Date date = new Date();


                for(int j = 1; j<=k; j++)
                    new BigInteger(bits, 100, r)

                println  "($k, $bits) Traditional prime genertion started: $date\n"
                date = new Date();
                println  "($k, $bits) Traditional prime genertion finished: $date\n"
                println "\n==========================================================\n"


            }

    }


    public static void main(String[] args) {

        def test = new Test()

        //test.compareTraditionalAndCRT()
        test.generatePrimes()




    }


}
