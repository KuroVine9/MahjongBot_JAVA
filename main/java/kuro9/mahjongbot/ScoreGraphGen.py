from matplotlib import font_manager, pyplot as plt
from PIL import Image

def scoreGraphGen(rank_array, sunglass_array):
    plt.rcdefaults()
    plt.rcParams.update({'axes.facecolor':'#273250'})

    imgarr=[37, 119, 202, 284, 367, 449, 532, 614, 697, 779]

    plt.figure(figsize=(10,3),facecolor='#273250')
    plt.axis([-0.2,9.2,0,5])
    ax=plt.subplot()

    ax.spines['right'].set_visible(False)
    ax.spines['left'].set_visible(False)
    ax.spines['top'].set_visible(False)
    ax.spines['bottom'].set_visible(False)
    ax.set_xticks([])
    ax.set_yticks([1,2,3,4])
    ax.set_yticklabels(['4th','3rd','2nd','1st'])

    ax.tick_params(width=0,labelcolor='w')
    ax.axhline(1,label='dfdf',color='#376977',linewidth=1)
    ax.axhline(2,label='3',color='#376977',linewidth=1)
    ax.axhline(3,label='2',color='#376977',linewidth=1)
    ax.axhline(4,label='1',color='#376977',linewidth=1)
    plt.plot(rank_array, marker='o',ms=10,mec='#ffc029',mfc='#ffc029',color='#d65f2a',linewidth=4)

    picname='score_graph.png'
        
    plt.savefig(f'image/{picname}',bbox_inches='tight')

    if 1 in sunglass_array:
        img=Image.open(f'image/{picname}').convert('RGBA')
        img2=Image.open('image/nyanglass_nuki.png').convert('RGBA')
        resize_img2=img2.resize((50,37))
        for i in range(len(sunglass_array)):
            if sunglass_array[i]==1:
                img.paste(resize_img2, (imgarr[i],38),resize_img2)
        img.save(f'image/{picname}','png')