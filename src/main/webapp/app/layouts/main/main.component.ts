import { Component, OnInit, inject, signal, AfterViewChecked, AfterViewInit, AfterContentInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';

import { AppPageTitleStrategy } from 'app/app-page-title-strategy';
import FooterComponent from '../footer/footer.component';
import PageRibbonComponent from '../profiles/page-ribbon.component';
import { NgbToast } from '@ng-bootstrap/ng-bootstrap';
import NavbarComponent from '../navbar/navbar.component';
@Component({
  standalone: true,
  selector: 'jhi-main',
  templateUrl: './main.component.html',
  providers: [AppPageTitleStrategy],
  imports: [RouterOutlet, FooterComponent, PageRibbonComponent, NgbToast, NavbarComponent],
  styleUrl: './main.component.scss',
})
export default class MainComponent {}
