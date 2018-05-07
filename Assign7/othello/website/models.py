from django.db import models

# Create your models here.
class Game(models.Model):
    token_no=models.AutoField(primary_key=True)
    board=models.CharField(max_length=66)
    count=models.IntegerField(default=0)
    black = models.BooleanField(default=False)
    white=models.BooleanField(default=False)
    player_turn=models.IntegerField(default=2)
    ctime = models.IntegerField(default=60)
    valid = models.BooleanField(default=True)
    over = models.CharField(max_length=255,default="no")
    winner=models.CharField(max_length=255,default='Draw')

    def __str__(self):
        return "Token No - %s , Game Board - %s, Count-%s, Black-%s, White-%s, Turn-%s" %(self.token_no,self.board,self.count,self.black,self.white,self.player_turn)
