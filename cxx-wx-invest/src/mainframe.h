#ifndef MAIN_FRAME_H
#define MAIN_FRAME_H

#include <wx/frame.h>

#include <gpLineLayer.h>
#include <gpPanel.h>
// #include <gpSeries.h>

#include "drawpane.h"

class CMainFrame : public wxFrame
{
private:
    // wxPointList mAssetPrice;
    // CDrawPane* mDrawPane;

    wxStaticText* mAssetValue;
    wxStaticText* mMoney;

    gpPanel* mGraphPanel;
    gpLineLayer* mLineLayer;
    gpSeries* mSeries;
    gpSeries* mSeriesFilter;
    mpWindow* mWindow;

    void loadAssetsFiles();
    void algorithm();

    mpPointLayer* addPoint(const wxString& name, const wxColour& colour, double x, double y);

    void OnMouseMove(wxMouseEvent& event);

    void bay(const double& time, const double& price);
    void sale(const double& time, const double& price, const size_t& index);

public:
    CMainFrame();
    ~CMainFrame() override;
};

#endif // MAIN_FRAME_H