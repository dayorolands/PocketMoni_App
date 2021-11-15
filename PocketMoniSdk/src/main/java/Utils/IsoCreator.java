package Utils;

public class IsoCreator {
    public String Field[] = new String[129];

    public IsoCreator(){
        for(int i=0; i<Field.length; i++){
            Field[i] = " ";
        }
    }

    public String getPackedISO(int bitmapLength)
    {
        String rawdata = "", bitmap = "", mti = ""; int i = 0;
        if (bitmapLength > 16)
        {
            Field[1] = "";
            bitmap += "1";
        }
        else { Field[1] = " "; }

        for(int u=0; u<Field.length; u++){
            if (u == 0) { mti = Field[u]; continue; }
            if (i++ >= (bitmapLength) * 4) break;
            if(Field[u] != " ")
            {
                if (i == 1) continue;
                if ((u == 64) || (u == 128))
                {
                    bitmap += "1";
                    String bitmapHex = Keys.binaryStringToHexString(bitmap);
                    String hash = Emv.sessionKey + Keys.asciiToHex(mti + bitmapHex + rawdata);
                    rawdata += Keys.SHA1Encrypt(hash, "02");
                }
                else
                {
                    bitmap += "1";

                    if(u == 2) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 32) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 33) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 35) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 54) rawdata += (Keys.padLeft(""+Field[u].length(),3,'0') + Field[u]);
                    else if(u == 55) rawdata += (Keys.padLeft(""+Field[u].length(),3,'0') + Field[u]);
                    else if(u == 56) rawdata += (Keys.padLeft(""+Field[u].length(),3,'0') + Field[u]);
                    else if(u == 59) rawdata += (Keys.padLeft(""+Field[u].length(),3,'0') + Field[u]);
                    else if(u == 62) rawdata += (Keys.padLeft(""+Field[u].length(), 3, '0') + Field[u]);
                    else if(u == 100) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 102) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 103) rawdata += (Keys.padLeft(""+Field[u].length(),2,'0') + Field[u]);
                    else if(u == 123) rawdata += (Keys.padLeft(""+Field[u].length(),3,'0') + Field[u]);
                    else rawdata += Field[u];
                }
            }
            else { bitmap += "0"; }
        }
        String hexBitmap = Keys.binaryStringToHexString(bitmap);
        String result = Keys.asciiToHex(mti + hexBitmap + rawdata);
        return (Keys.padLeft(Integer.toString((result.length()/2), 16), 4, '0') + result).toUpperCase();
    }
}