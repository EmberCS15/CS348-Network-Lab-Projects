# -*- coding: utf-8 -*-
# Generated by Django 1.11.6 on 2018-04-08 05:12
from __future__ import unicode_literals

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('website', '0005_auto_20180407_1940'),
    ]

    operations = [
        migrations.AlterField(
            model_name='game',
            name='token_no',
            field=models.AutoField(primary_key=True, serialize=False),
        ),
    ]