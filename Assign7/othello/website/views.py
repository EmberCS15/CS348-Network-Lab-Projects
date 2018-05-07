from django.shortcuts import render,redirect
from .models import Game
import datetime,json,time,threading
from django.http.response import JsonResponse

turn=None
timer=datetime.datetime.now()
over = 'no'
def index(request):
    template_name = 'website/index.html'
    context = {}
    return render(request,template_name,context)

def gameboard(request,token):
    global turn,timer
    print('In The Function')
    t = int(token)
    d = datetime.datetime.now()
    d=d+datetime.timedelta(0,52)
    timer=d
    print('Starting to dump : %s' %(timer))
    my_dict  = {'date':str(d.day),'year':str(d.year),'month':str(d.month),'hour':str(d.hour),'minute':str(d.minute),'second':str(d.second)}
    json_data = json.dumps(my_dict)
    print('Dumped data')
    g = -1
    if(t>=0):
        try:
            g=Game.objects.get(token_no=t)
        except Exception as e:
            print('Please enter a valid token')
            return render(request,'website/index.html',{'error_message':'Please enter a valid token'})
    else:
        print('Hello New Game')
        s = ''
        for i in range(1,65):
            if i==28 or i==37:
                s=s+'2'
            elif i==29 or i==36:
                s=s+'1'
            else:
                s=s+'0'
        g=Game.objects.create(board=s,count=1,white=True,black=False,player_turn=2,ctime=30,valid=True,over='no')
        turn=2
    print("Game : %s" %(g))
    print('token %s, t= %s' %(g.token_no,t))
    print('json data : %s' %(json_data))
    context={'json_data':json_data,'token':g.token_no,'board':g.board,'game':g,'message':'Play','turn':turn,'myid':1,'ctime':g.ctime}
    return render(request, 'website/othello_game.html', context)

def joingame(request):
    global turn,timer
    t=request.GET['token']
    t=int(t)
    id=2
    d=timer
    print('d = %s' %(d))
    my_dict = {'date': str(d.day), 'year': str(d.year), 'month': str(d.month), 'hour': str(d.hour),
               'minute': str(d.minute), 'second': str(d.second)}
    json_data = json.dumps(my_dict)
    #print('Dumped data')
    print('token retrieved : %s' %(t))
    g=-1
    try:
        g=Game.objects.get(token_no=t)
        print("g=%s" %(g))
        if g.count==2:
            return render(request, 'website/index.html', {'error_message': 'Session Full for token entered'})
        if g.count==0:
            g.white = True
            id=1
        elif g.count==1:
            if not g.black:
                g.black=True
                id=2
            else:
                g.white=True
                id=1
            print('Thread creation.....')
            threading.Thread(target=server_timer,args=(t,)).start()
            print("Thread Started")
        #add blocking feature if not turn of incoming player
        #increment count by 1 and save g
        g.count=g.count+1
        ####################################################
        turn=g.player_turn
        g.save()
        context={'json_data':json_data,'token':g.token_no,'board':g.board,'game':g,'message':'Play','turn':turn,'myid':id,'ctime':g.ctime}
        #print('Hello Fine Here')
        return render(request,'website/othello_game.html',context)
    except Exception as e:
        print('could not retrieve token %s from data base ' %(t))
        return render(request,'website/index.html',{'error_message':'Please enter a valid token'})

def done(request):
    global turn,over
    token=None
    brd=None
    v = ''
    if request.method=='POST':
        brd=request.POST['board']
        token=request.POST['token']
        v = request.POST['validity']
    print('Validity : %s' %(v))
    t = int(token)
    if v == 'true':
        v = True
    else:
        v = False
    try:
        g=Game.objects.get(token_no=t)
        if checkCondition(g.valid,v,brd,g)==True:
            print('1.Game over')
            over='yes'
            g.over=over
            g.board=brd
            over='no'
            g.save()
            return JsonResponse({'board':g.board,'over': g.over, 'winner': g.winner})
        turn = int(g.player_turn)
        if turn==1:
            turn=2
        else:
            turn=1
        print("Token : %s , Board: %s, Turn = %s, Actual=%s" % (t, brd,g.player_turn,turn))
        print("g=%s" %(g))
        g.board=str(brd)
        g.player_turn=int(turn)
        g.ctime = 30
        g.valid = v
        g.save()
        myDict = {'turn':g.player_turn,'board':g.board,'count':g.count,'black':g.black,'white':g.white,'token':g.token_no,'ctime':g.ctime,'over':g.over}
        return JsonResponse(myDict)
    except Exception as e:
        print('could not retrieve token %s from data base ' %(t))
        return render(request,'website/index.html',{'error_message':'Please enter a valid token'})

def server_request(request):
    global over
    token=-1
    if request.method=='POST':
        token=request.POST['token']
    t = int(token)
    try:
        g=Game.objects.get(token_no=t)
        if g.over == 'yes':
            print('2.Game Over = %s' %(t))
            return JsonResponse({'board':g.board,'over': g.over, 'winner': g.winner})
        #print("Turn : %s" %(g.player_turn))
        my_dict = {'token':g.token_no,'board':g.board,'count':g.count,'black':g.black,'white':g.white,'turn':g.player_turn,'ctime':g.ctime,'over':g.over}
        return JsonResponse(my_dict)
    except Exception as e:
        print('could not retrieve token %s from data base ' %(t))

def server_timer(token):
    t=int(token)
    g=-1
    while True:
        time.sleep(1)
        try:
            g = Game.objects.get(token_no=t)
            if g.over == 'yes':
                return
        except Exception as e:
            print('Could not retreive the game with token %s' % (token))
        g.ctime=g.ctime-1
        g.save()

def checkCondition(v1,v2,brd,g):
    print("v1 = %s , v2 =%s" %(v1,v2))
    rp=v1 or v2
    brp=True
    winner=[0,0,0]
    for k in range(1,64):
        winner[int(brd[k-1])]+=1
        if brd[k-1]=='0':
            brp=False
    if winner[1]<winner[2]:
        g.winner='Black'
    elif winner[2]<winner[1]:
        g.winner='White'
    else:
        g.winner='Draw'
    print('The Winner is %s' %(g.winner))
    g.save()
    if not rp:
        return True
    return brp












