from PIL import Image

class LSB():

	def makeImageEven(self,image):
		pixels = list(image.getdata())
		evenPixels = [(r>>1<<1,g>>1<<1,b>>1<<1,t>>1<<1) for [r,g,b,t] in pixels]
		evenImage = Image.new(image.mode, image.size)
		evenImage.putdata(evenPixels)
		return evenImage

	def constLenBin(self,int):
		binary = "0"*(8-(len(bin(int))-2)) + bin(int).replace('0b','')
		return binary

	def encodeDataInImage(self,image, data):
		evenImage = self.makeImageEven(image)
		binary = ''.join(map(self.constLenBin,bytearray(data,'utf-8')))
		if len(binary) > len(image.getdata()) * 4:
			return -1
		encodedPixels = [(r + int(binary[index*4+0]),g + int(binary[index*4+1]),
			b + int(binary[index*4 + 2]),t + int(binary[index*4 + 3]))
			if index*4 < len(binary) else (r,g,b,t) for index,(r,g,b,t) in enumerate(list(evenImage.getdata()))]
		encodedImage = Image.new(evenImage.mode, evenImage.size)
		encodedImage.putdata(encodedPixels)
		return encodedImage

	def binaryToString(self,binary):
		index = 0
		string = []
		rec = lambda x, i: x[2:8] + (rec(x[8:], i-1) if i > 1 else '') if x else ''
		# rec = lambda x, i: x and (x[2:8] + (i > 1 and rec(x[8:], is-1) or '')) or ''
		fun = lambda x, i: x[i+1:8] + rec(x[8:], i-1)
		while index + 1 < len(binary):
			chartype = binary[index:].index('0')
			length = chartype*8 if chartype else 8
			string.append(chr(int(fun(binary[index:index+length],chartype),2)))
			index += length
		return ''.join(string)

	def decodeImage(self,image):
		pixels = list(image.getdata())  # 获得像素列表 
		binary = ''.join([str(int(r>>1<<1 != r))+str(int(g>>1<<1 != g))+str(int(b>>1<<1 != b)) + str(int(t>>1<<1 != t)) for (r,g,b,t) in pixels])
		# 提取图片中所有最低有效位中的数据
		# 找到数据截止处的索引
		locationDoubleNull = binary.find('0000000000000000')
		endIndex = locationDoubleNull+(8-(locationDoubleNull % 8)) if locationDoubleNull%8 != 0 else locationDoubleNull
		data = self.binaryToString(binary[0:endIndex])
		return data


lsb = LSB()

encodeImageName = 'aout.png'
encodeData = 'Hello world!'
encodeSaveName = 'fin.png'

lsb.encodeDataInImage(Image.open(encodeImageName),encodeData).save(encodeSaveName)

#decodeImageName = 'fin.png'

#print(lsb.decodeImage(Image.open(decodeImageName)))

