#include "mainframe.h"

#include <cfloat>
#include <wx/dir.h>
#include <wx/sizer.h>
#include <wx/tokenzr.h>
#include <wx/txtstrm.h>
#include <wx/wfstream.h>

#define DEF_RECT wxDefaultPosition, wxDefaultSize

// #define SIZE_FRAME wxSize(800, 600)
#define SIZE_FRAME wxSize(1000, 1000)

#define BEGIN_Y 18700

CMainFrame::CMainFrame()
    : wxFrame(nullptr, NewControlId(), wxT("Invest"), wxPoint(50, 50), SIZE_FRAME)
{
    // wxPanel* panel = new wxPanel(this);

    mGraphPanel = new gpPanel(this, wxNewId(), wxDefaultPosition, wxSize(240, 336));
    mLineLayer = new gpLineLayer(_("Line"), _("x-label"), _("y-label"));
    mSeries = mLineLayer->AddSeriesLayer("Random");
    mSeriesFilter = mLineLayer->AddSeriesLayer("Filter");
    mSeriesFilter->SetPen(wxPen(*wxGREEN));

    mGraphPanel->AddLayer(mLineLayer, POPUP_FILE | POPUP_CHART | POPUP_EDIT | POPUP_HELP | POPUP_FIT);
    mWindow = mGraphPanel->GetWindowByLayer(mLineLayer);
    mWindow->SetGradienBackColour(false);

    mAssetValue = new wxStaticText(mWindow, NewControlId(), "");

    mMoney = new wxStaticText(mWindow, NewControlId(), "", wxPoint(this->GetSize().x - 100, 20));

    mLineLayer->RefreshChart();
    mGraphPanel->Fit(mLineLayer);

    /*mDrawPane = new CDrawPane(this);


    wxBoxSizer* main_box = new wxBoxSizer(wxHORIZONTAL);
    main_box->Add(mDrawPane, 1, wxEXPAND);
    this->SetSizer(main_box);

    this->SetAutoLayout(true);

    */

    mWindow->Bind(wxEVT_MOTION, &CMainFrame::OnMouseMove, this);

    this->CallAfter(&CMainFrame::loadAssetsFiles);
}

CMainFrame::~CMainFrame()
{
    /*for(auto& p : mAssetPrice)
        delete p;*/
}

void CMainFrame::OnMouseMove(wxMouseEvent& event)
{
    wxPoint winPoint = mWindow->GetPosition();

    // wxPoint point = event.GetPosition();
    // double value = (BEGIN_Y - point.y) / 100.0;

    wxPoint mousePoint = event.GetPosition();

    double xd = mWindow->p2x(mousePoint.x);
    double yd = mWindow->p2y(mousePoint.y);

    mAssetValue->SetPosition(wxPoint(mousePoint.x + 5, mousePoint.y - 15));
    mAssetValue->SetLabel(wxString::Format("x=%.2f, y=%.2f", xd, yd));
}

void CMainFrame::loadAssetsFiles()
{
    int i = 0;
    bool count;

    int x = 0;
    int y = 0;
    wxString filename;
    const wxString dirname = wxT("../../resources/BBG000BBJQV0_2022");

    double high = 0;
    double low = 0;

    wxYield();

    wxDir dir(dirname);
    if(dir.IsOpened()) {

        count = dir.GetFirst(&filename);
        while(count == true) {

            wxFileInputStream input(dirname + '/' + filename);
            wxTextInputStream text(input); // wxTextInputStream text(input, wxT("\x09"), wxConvUTF8 );
            while(input.IsOk() && !input.Eof()) {
                i = 0;
                wxStringTokenizer array(text.ReadLine(), ';');
                while(array.HasMoreTokens()) {

                    wxString token = array.GetNextToken();
                    if(i == 4) {
                        token.ToDouble(&high);
                    }

                    if(i == 5) {
                        token.ToDouble(&low);
                    }

                    if(i == 6) {

                        // y = BEGIN_Y - (((high + low) / 2 * 1000) / 10);
                        // mAssetPrice.Append(new wxPoint(x++, y));

                        mSeries->DataPush(x++, (high + low) / 2);
                        break;
                    }

                    i++;
                }
            }

            count = dir.GetNext(&filename);
        }

    } else
        std::cout << "error: dir not open: " << dirname << std::endl;

    mGraphPanel->Refresh();

    algorithm();
}

mpPointLayer* CMainFrame::addPoint(const wxString& name, const wxColour& colour, double x, double y)
{
    mpPointLayer* pointLayer = mLineLayer->AddSinglePoint(name, "Random");
    if(pointLayer != nullptr) {
        pointLayer->ShowName(true);
        pointLayer->SetPen(wxPen(colour));
        pointLayer->SetBrush(colour);
        pointLayer->EnableModify(false);
        pointLayer->SetVisible(true);
        pointLayer->SetPosition(x, y);

        mWindow->AddLayer(pointLayer);
        mGraphPanel->Refresh();
        wxYield();
    }
    return pointLayer;
}

// -----------------------------------------------------------------------

struct TAsset {

    double price = 0.0;
    double commission = 0.0;
    double order = 0.0;

    TAsset(const double& bay)
    {
        price = bay;
        commission = bay * 0.05;
        order = bay + 10.0;
    }
};

struct TAssetWithoutCommission {

    double price;
    double order;
    double early = 0.0;

    TAssetWithoutCommission(const double& bay)
        : price(bay)
        , order(bay + 1.0)
    {
        early = bay;
    }
};

// std::vector<TAsset> asset;
std::vector<TAssetWithoutCommission> asset2;
double money = 100000.0;

void CMainFrame::bay(const double& time, const double& price)
{
    addPoint(wxString::Format("%.2f", price), *wxGREEN, time, price);
    money -= price * 10;
    asset2.emplace_back(price);

    mMoney->SetLabel(wxString::Format("%.2f", money));
}

void CMainFrame::sale(const double& time, const double& price, const size_t& index)
{
    addPoint(wxString::Format("%.2f", price), *wxRED, time, price);
    money += price * 10;
    asset2.erase(asset2.begin() + (int)index);

    mMoney->SetLabel(wxString::Format("%.2f", money));
}

#define SIZE_FILTER 5
double filter[SIZE_FILTER] = { 0 };

void CMainFrame::algorithm()
{
    wxYield();

    auto data = mSeries->GetData();

    double time = 0.0;
    double price = 0.0;
    double filterPrice = 0.0;
    double threshold = 0.0;
    const double increment = 10.0;

    time = data.begin()->first;
    price = threshold = data.begin()->second;

    // first bay
    // bay(time, price);

    double earlyPrice = price;
    double earlyTime = time;

    double max = -DBL_MAX;
    double min = DBL_MAX;

    bool toggle = true;

    for(auto& p : data) {

        time = p.first;
        price = p.second;

        for(int i = 0; i < SIZE_FILTER - 1; i++)
            filter[i] = filter[i + 1];
        filter[SIZE_FILTER - 1] = price;

        double sum = 0.0;
        for(double& i : filter)
            sum += i;
        filterPrice = sum / SIZE_FILTER;

        filterPrice = price;

        /*filter[0] = filter[1];
        filter[1] = filter[2];
        filter[2] = filter[3];
        filter[3] = filter[4];
        filter[4] = filter[5];
        filter[5] = price;

        price = (filter[0] + filter[1] + filter[2] + filter[3] + filter[4] + filter[5]) / 6;*/

        if(filterPrice < 160)
            continue;

        // mSeriesFilter->DataPush(time, filterPrice);

        /** One */

        /*if(price > threshold + increment && money > price) {
            threshold = price;
            bay(time, price);
        }

        if(price < threshold - increment) {
            threshold = price;
        }

        for(auto itr = asset.begin(); itr != asset.end();) {

            if(itr->order < price) {
                sale(time, price, std::distance(asset.begin(), itr));
            } else
                ++itr;
        }*/

        /*if(299.0 < time && time < 301.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(309.0 < time && time < 311.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));*/

        if(334.0 < time && time < 336.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(1069.0 < time && time < 1071.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(1172.0 < time && time < 1174.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(1814.0 < time && time < 1816.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(2055.0 < time && time < 2057.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(2292.0 < time && time < 2294.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        if(2685.0 < time && time < 2687.0)
            for(auto itr = asset2.begin(); itr != asset2.end();)
                sale(time, itr->price, std::distance(asset2.begin(), itr));

        /** Two */

        double incline = (filterPrice - earlyPrice) / (time - earlyTime);

        if(incline < -0.0) {

            if(earlyPrice > max)
                max = earlyPrice;

            /*for(auto itr = asset2.begin(); itr != asset2.end();) {
                if(itr->price + 0.1 > price)
                    sale(time, price, std::distance(asset2.begin(), itr));
                else
                    ++itr;
            }*/

            if((max - 0.1) > filterPrice) { //  && price > (max - 0.2)) {
                for(auto itr = asset2.begin(); itr != asset2.end();) {

                    if(price > itr->price)
                        sale(time, price, std::distance(asset2.begin(), itr));
                    else
                        ++itr;
                }

                min = DBL_MAX;
            }

            /*if((earlyPrice - price) > 0.1) {

                if (price > max)
                    max = price;

                if((max - 0.1) > price) {// && price > (max - 0.3)) {
                    for(auto itr = asset2.begin(); itr != asset2.end();) {
                        sale(time, price, std::distance(asset2.begin(), itr));
                    }
                }

                min = DBL_MAX;
            }*/
        }

        if(incline > 0.0) {

            if(earlyPrice < min)
                min = earlyPrice;

            if((min + 0.1) < filterPrice) { // && price < (min + 0.2)) {
                if(asset2.empty() == true)
                    bay(time, price);
                else {
                    /*for(auto itr = asset2.begin(); itr != asset2.end();) {

                        if(filterPrice < itr->price)
                            sale(time, price, std::distance(asset2.begin(), itr));
                        else
                            ++itr;
                    }*/
                }

                max = -DBL_MAX;
            }

            /*if((price - earlyPrice) > 0.3) {

                if(price < min)
                    min = price;

                if((min + 0.1) < price) // && price < (min + 0.3))
                    bay(time, price);

                max = -DBL_MAX;
            }*/
        }

        /*if (price >= max) {
            max = price;
        } else {
            if(max - price > 0.5) {

            }
        }

        if(price <= min) {
            min = price;
        } else {
            if(price - min > 0.2) {
                bay(time, price);
            }
        }

        for(auto itr = asset2.begin(); itr != asset2.end();) {

            if(price >= itr->early) {
                itr->early = price;
            } else {
                if(price > itr->order) {

                    if (itr->early - price > 0.1) {
                        sale(time, price, std::distance(asset2.begin(), itr));
                        continue;
                    }
                }
            }

            ++itr;
        }*/

        earlyPrice = filterPrice;
        earlyTime = time;
    }
}