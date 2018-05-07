from django.conf.urls import url
from . import views

app_name = 'website'

urlpatterns = [
    url(r'^$',views.index,name='index'),
    url(r'^game/(?P<token>-?[0-9]+)/$',views.gameboard,name='game'),
    url(r'^joingame/$',views.joingame,name='joingame'),
    url(r'^done/$',views.done,name='done'),
    url(r'^server_request/$',views.server_request,name='server_request')
]
